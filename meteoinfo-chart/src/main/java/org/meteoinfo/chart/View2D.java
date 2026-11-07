package org.meteoinfo.chart;

/**
 * The top-down two dimensional view, equivalent to MATLAB {@code view(2)}.
 *
 * @author MeteoInfo
 */
public final class View2D implements AxesView {

    public static final View2D INSTANCE = new View2D();

    private View2D() {
    }

    @Override
    public boolean is3D() {
        return false;
    }

    @Override
    public AxesView copy() {
        return this;
    }

    @Override
    public String describe() {
        return "2D";
    }

    @Override
    public String toString() {
        return "View2D";
    }
}
