package org.meteoinfo.chart.transform;

/**
 * Listener callback for transform invalidation event.
 * Used for non‑Composite containers such as TransformWrapper.
 */
public interface TransformListener {
    /**
     * Called when observed transform marks itself invalid.
     */
    void onTransformInvalidated();
}
