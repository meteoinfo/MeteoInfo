package org.meteoinfo.chart.transform;

import org.meteoinfo.common.PointD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Composite Transform: Chains two transforms together
public class CompositeTransform extends Transform {

    /**
     * Forward list of child transforms, immutable after construction,
     * corresponds to matplotlib _transforms tuple.
     */
    private final List<Transform> children;

    /**
     * Constructor
     */
    public CompositeTransform(Transform... transforms) {
        super();
        children = new ArrayList<>(Arrays.asList(transforms));

        for (Transform child : children) {
            child.addParent(this);
            if (child.isInvalid()) {
                this.invalidate();
            }
        }
    }

    @Override
    public PointD transform(PointD p) {
        PointD res = p;
        for (Transform t : children) {
            res = t.transform(res);
        }
        // After evaluate, mark self clean
        clearInvalid();
        return res;
    }

    @Override
    public boolean isAffine() {
        for (Transform t : children) {
            if (!t.isAffine()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Dispose composite transform. Remove all reverse parent references from children.
     * Matplotlib uses weakref GC; Java requires explicit dispose() to prevent memory leak.
     */
    public void dispose() {
        for (Transform child : children) {
            child.removeParent(this);
        }
        parents.clear();
        children.clear();
    }

    /**
     * Get read‑only copy of child transform list.
     * @return list of child transforms
     */
    public List<Transform> getChildren() {
        return new ArrayList<>(children);
    }

    public boolean isXShear() {
        for (Transform t : children) {
            if (t instanceof CompositeTransform) {
                if (((CompositeTransform) t).isXShear()) {
                    return true;
                }
            } else {
                if (t instanceof Affine2D) {
                    if (((Affine2D) t).affineTransform.getShearX() != 0) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

}
