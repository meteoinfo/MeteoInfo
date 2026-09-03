package org.meteoinfo.chart.transform;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Low‑level internal base class, corresponds to matplotlib TransformNode.
 * Provides dependency‑graph infrastructure: invalid(dirty) flag and reverse‑parent reference set.
 * Does NOT define transform business methods.
 * Subclass: Transform (public API layer).
 */
public abstract class TransformNode {
    /**
     * Invalid (dirty) flag. Equivalent to matplotlib _invalid.
     * True: internal state changed, cached result is stale.
     */
    protected boolean invalid = true;

    /**
     * Reverse‑reference set: stores all parent composite transforms referencing this node.
     * When invalidate() is called, dirty flag propagates upward to all parents.
     */
    protected final Set<CompositeTransform> parents = new HashSet<>();

    /**
     * External listeners (non‑Composite observers e.g. TransformWrapper).
     */
    private final List<TransformListener> listeners = new ArrayList<>();

    /**
     * Check whether current node is marked invalid (dirty).
     * @return true if dirty
     */
    public boolean isInvalid() {
        return invalid;
    }

    /**
     * Mark self invalid and propagate dirty state recursively to all parent composites.
     * Equivalent to matplotlib TransformNode.invalidate().
     */
    public void invalidate() {
        if (!this.invalid) {
            this.invalid = true;
            // notify composite parents
            Set<CompositeTransform> snapshotParents = new HashSet<>(parents);
            for (CompositeTransform parent : snapshotParents) {
                parent.invalidate();
            }
            // fire external listeners (TransformWrapper etc.)
            List<TransformListener> snapshotListeners = new ArrayList<>(listeners);
            for (TransformListener listener : snapshotListeners) {
                listener.onTransformInvalidated();
            }
        }
    }

    /**
     * Clear invalid flag after successful computation / evaluation.
     */
    protected void clearInvalid() {
        this.invalid = false;
    }

    /**
     * Register a parent composite to this child node.
     * Called during CompositeTransform construction.
     * @param parent parent composite transform
     */
    public void addParent(CompositeTransform parent) {
        parents.add(parent);
    }

    /**
     * Remove parent reference for resource disposal, avoid memory leak.
     * @param parent parent composite transform
     */
    public void removeParent(CompositeTransform parent) {
        parents.remove(parent);
    }

    /**
     * Add external invalidation listener (for non‑Composite observer like TransformWrapper).
     * @param listener listener instance
     */
    public void addTransformListener(TransformListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Remove external invalidation listener.
     * @param listener listener instance
     */
    public void removeTransformListener(TransformListener listener) {
        listeners.remove(listener);
    }
}
