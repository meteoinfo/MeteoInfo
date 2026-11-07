package org.meteoinfo.chart;

import org.meteoinfo.chart.plot.Plot;

/**
 * View description of an {@link Axes}.  The view controls whether the axes is
 * displayed as a 2-D or a 3-D plotting area.
 *
 * @author MeteoInfo
 */
public interface AxesView {

    /**
     * Whether this view is three dimensional.
     *
     * @return boolean
     */
    boolean is3D();

    /**
     * Create an independent copy of the view.
     *
     * @return view copy
     */
    AxesView copy();

    /**
     * Short description used by status bars and debugging.
     *
     * @return description
     */
    String describe();

    /**
     * Apply the view parameters to a plot.  The default implementation is a
     * no-op, which is correct for a 2-D view.
     *
     * @param plot plot
     * @return the plot
     */
    default Plot applyTo(Plot plot) {
        return plot;
    }
}
