package org.meteoinfo.chart.transform;


import org.meteoinfo.common.PointD;
import org.meteoinfo.common.PointZ;

/**
 * TransformWrapper: proxy wrapper for Transform instance.
 * The wrapper object reference remains unchanged; inner wrapped Transform can be swapped at runtime.
 * Invalidation propagates from inner delegate outward to wrapper's parents.
 * Matches our Transform / TransformNode dependency‑graph architecture.
 */
public class TransformWrapper extends Transform implements TransformListener {

    private Transform innerTransform;

    /**
     * Construct wrapper with initial inner transform delegate.
     * @param inner initial wrapped transform, can be null
     */
    public TransformWrapper(Transform inner) {
        super();
        this.innerTransform = inner;
        if (inner != null) {
            inner.addTransformListener(this);
            if (inner.isInvalid()) {
                this.invalidate();
            }
        }
    }

    /**
     * Replace the wrapped inner transform at runtime.
     * Properly update parent reverse‑reference graph.
     * @param newInner new transform delegate
     */
    public void setInnerTransform(Transform newInner) {
        // detach old inner from this wrapper
        if (this.innerTransform != null) {
            this.innerTransform.removeTransformListener(this);
        }

        this.innerTransform = newInner;

        if (newInner != null) {
            newInner.addTransformListener(this);
            if (newInner.isInvalid()) {
                this.invalidate();
            }
        } else {
            this.invalidate();
        }
    }

    /**
     * Get current wrapped inner transform.
     * @return inner delegate, may return null
     */
    public Transform getInnerTransform() {
        return innerTransform;
    }

    /**
     * Callback: inner transform becomes invalid → mark wrapper invalid.
     */
    @Override
    public void onTransformInvalidated() {
        this.invalidate();
    }

    @Override
    public PointZ transform(PointZ p) {
        if (innerTransform == null) {
            throw new IllegalStateException("TransformWrapper inner transform is null");
        }
        PointZ res = innerTransform.transform(p);
        clearInvalid();
        return res;
    }

    @Override
    public Transform inverted() {
        if (innerTransform == null) {
            throw new IllegalStateException("TransformWrapper inner transform is null");
        }
        // Return new wrapper wrapping inverted inner transform
        return new TransformWrapper(innerTransform.inverted());
    }

    @Override
    public boolean isAffine() {
        if (innerTransform == null) {
            return false;
        }
        return innerTransform.isAffine();
    }

    /**
     * Dispose wrapper: unsubscribe listener, clear reference.
     */
    public void dispose() {
        if (innerTransform != null) {
            innerTransform.removeTransformListener(this);
            innerTransform = null;
        }
        parents.clear();
    }
}

