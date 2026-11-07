package org.meteoinfo.chart;

import org.meteoinfo.chart.jogl.GLPlot;
import org.meteoinfo.chart.plot.Plot;
import org.meteoinfo.chart.plot.Plot3D;
import org.meteoinfo.chart.plot3d.Projector;

/**
 * A three dimensional camera view, equivalent to MATLAB
 * {@code view(azimuth, elevation)}.
 *
 * @author MeteoInfo
 */
public class View3D implements AxesView {

    private double azimuth;
    private double elevation;
    private double roll;
    private boolean orthographic = true;
    private double distance = 5.0;
    private double fieldOfView = 45.0;

    public View3D() {
        this(-45.0, 45.0);
    }

    public View3D(double azimuth, double elevation) {
        this.azimuth = azimuth;
        this.elevation = elevation;
    }

    public View3D(double azimuth, double elevation, double roll,
                  boolean orthographic, double distance, double fieldOfView) {
        this(azimuth, elevation);
        this.roll = roll;
        this.orthographic = orthographic;
        this.distance = distance;
        this.fieldOfView = fieldOfView;
    }

    public static View3D from(GLPlot plot) {
        View3D view = new View3D(plot.getAngleX(), plot.getAngleY());
        view.roll = plot.getHeadAngle();
        view.orthographic = plot.isOrthographic();
        view.distance = plot.getDistance();
        view.fieldOfView = plot.getFieldOfView();
        return view;
    }

    public static View3D from(Plot3D plot) {
        Projector projector = plot.getProjector();
        return new View3D(projector.getRotationAngle(), projector.getElevationAngle());
    }

    @Override
    public Plot applyTo(Plot plot) {
        if (plot instanceof GLPlot) {
            GLPlot glPlot = (GLPlot) plot;
            if (glPlot.getAngleX() != (float) azimuth) {
                glPlot.setAngleX((float) azimuth);
            }
            if (glPlot.getAngleY() != (float) elevation) {
                glPlot.setAngleY((float) elevation);
            }
            if (glPlot.getHeadAngle() != (float) roll) {
                glPlot.setHeadAngle((float) roll);
            }
            if (glPlot.isOrthographic() != orthographic) {
                glPlot.setOrthographic(orthographic);
            }
            if (glPlot.getDistance() != (float) distance) {
                glPlot.setDistance((float) distance);
            }
            if (glPlot.getFieldOfView() != (float) fieldOfView) {
                glPlot.setFieldOfView((float) fieldOfView);
            }
        } else if (plot instanceof Plot3D) {
            Projector projector = ((Plot3D) plot).getProjector();
            projector.setRotationAngle((float) azimuth);
            projector.setElevationAngle((float) elevation);
            projector.setDistance((float) distance);
        }
        return plot;
    }

    @Override
    public boolean is3D() {
        return true;
    }

    @Override
    public AxesView copy() {
        return new View3D(azimuth, elevation, roll, orthographic, distance, fieldOfView);
    }

    @Override
    public String describe() {
        return String.format("3D(az=%.1f, el=%.1f, roll=%.1f)", azimuth, elevation, roll);
    }

    public double getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(double azimuth) {
        this.azimuth = azimuth;
    }

    public double getElevation() {
        return elevation;
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

    public double getRoll() {
        return roll;
    }

    public void setRoll(double roll) {
        this.roll = roll;
    }

    public boolean isOrthographic() {
        return orthographic;
    }

    public void setOrthographic(boolean orthographic) {
        this.orthographic = orthographic;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getFieldOfView() {
        return fieldOfView;
    }

    public void setFieldOfView(double fieldOfView) {
        this.fieldOfView = fieldOfView;
    }

    @Override
    public String toString() {
        return describe();
    }
}
