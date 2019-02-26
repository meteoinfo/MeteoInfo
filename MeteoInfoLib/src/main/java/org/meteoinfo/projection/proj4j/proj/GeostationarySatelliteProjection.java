/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.projection.proj4j.proj;

import org.meteoinfo.projection.proj4j.ProjCoordinate;
import org.meteoinfo.projection.proj4j.ProjectionException;
import org.meteoinfo.projection.proj4j.util.ProjectionMath;

/**
 *
 * @author yaqiang
 */
public class GeostationarySatelliteProjection extends Projection {

    private double _radiusP;
    private double _radiusP2;
    private double _radiusPInv2;
    private double _radiusG;
    private double _radiusG1;
    private double _c;

    /**
     * Constructor
     */
    public GeostationarySatelliteProjection() {
        proj4Name = "geos";
        name = "Geostationary";
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        _radiusG = 1 + (_radiusG1 = heightOfOrbit / a);
        _c = _radiusG * _radiusG - 1.0;
        if (!this.spherical) {
            _radiusP = Math.sqrt(one_es);
            _radiusP2 = one_es;
            _radiusPInv2 = rone_es;
        } else {
            _radiusP = _radiusP2 = _radiusPInv2 = 1.0;
        }
    }

    @Override
    public ProjCoordinate project(double lplam, double lpphi, ProjCoordinate out) {
        if (spherical) {
            project_s(lplam, lpphi, out);
        } else {
            project_e(lplam, lpphi, out);
        }
        return out;
    }

    public void project_s(double lplam, double lpphi, ProjCoordinate out) {
        /* Calculation of the three components of the vector from satellite to
         ** position on earth surface (lon,lat).*/
        double tmp = Math.cos(lpphi);
        double vx = Math.cos(lplam) * tmp;
        double vy = Math.sin(lplam) * tmp;
        double vz = Math.sin(lpphi);

        /* Check visibility.*/
        if (((_radiusG - vx) * vx - vy * vy - vz * vz) < 0) {
            out.x = Double.NaN;
            out.y = Double.NaN;
            //throw new ProjectionException(20);
            return;
        }

        /* Calculation based on view angles from satellite.*/
        tmp = _radiusG - vx;
        out.x = _radiusG1 * Math.atan(vy / tmp);
        out.y = _radiusG1 * Math.atan(vz / ProjectionMath.hypot(vy, tmp));
    }

    public void project_e(double lplam, double lpphi, ProjCoordinate out) {
        /* Calculation of geocentric latitude. */
        lpphi = Math.atan(_radiusP2 * Math.tan(lpphi));

        /* Calculation of the three components of the vector from satellite to
         ** position on earth surface (lon,lat).*/
        double r = (_radiusP) / ProjectionMath.hypot(_radiusP * Math.cos(lpphi), Math.sin(lpphi));
        double vx = r * Math.cos(lplam) * Math.cos(lpphi);
        double vy = r * Math.sin(lplam) * Math.cos(lpphi);
        double vz = r * Math.sin(lpphi);

        /* Check visibility. */
        if (((_radiusG - vx) * vx - vy * vy - vz * vz * _radiusPInv2) < 0) {
            out.x = Double.NaN;
            out.y = Double.NaN;
            //throw new ProjectionException(20);
            return;
        }

        /* Calculation based on view angles from satellite. */
        double tmp = _radiusG - vx;
        out.x = _radiusG1 * Math.atan(vy / tmp);
        out.y = _radiusG1 * Math.atan(vz / ProjectionMath.hypot(vy, tmp));
    }

    @Override
    public ProjCoordinate projectInverse(double xyx, double xyy, ProjCoordinate out) {
        if (spherical) {
            projectInverse_s(xyx, xyy, out);
        } else {
            projectInverse_e(xyx, xyy, out);
        }
        return out;
    }

    public void projectInverse_s(double xyx, double xyy, ProjCoordinate out) {
        double det;

        /* Setting three components of vector from satellite to position.*/
        double vx = -1.0;
        double vy = Math.tan(xyx / (_radiusG - 1.0));
        double vz = Math.tan(xyy / (_radiusG - 1.0)) * Math.sqrt(1.0 + vy * vy);

        /* Calculation of terms in cubic equation and determinant.*/
        double a = vy * vy + vz * vz + vx * vx;
        double b = 2 * _radiusG * vx;
        if ((det = (b * b) - 4 * a * _c) < 0) {
            throw new ProjectionException();
        }

        /* Calculation of three components of vector from satellite to position.*/
        double k = (-b - Math.sqrt(det)) / (2 * a);
        vx = _radiusG + k * vx;
        vy *= k;
        vz *= k;

        /* Calculation of longitude and latitude.*/
        double lplam = Math.atan2(vy, vx);
        double lpphi = Math.atan(vz * Math.cos(lplam) / vx);

        out.x = lplam;
        out.y = lpphi;
    }

    public void projectInverse_e(double xyx, double xyy, ProjCoordinate out) {
        double det;

        /* Setting three components of vector from satellite to position.*/
        double vx = -1.0;
        double vy = Math.tan(xyx / _radiusG1);
        double vz = Math.tan(xyy / _radiusG1) * ProjectionMath.hypot(1.0, vy);

        /* Calculation of terms in cubic equation and determinant.*/
        double a = vz / _radiusP;
        a = vy * vy + a * a + vx * vx;
        double b = 2 * _radiusG * vx;
        if ((det = (b * b) - 4 * a * _c) < 0) {
            throw new ProjectionException();
        }

        /* Calculation of three components of vector from satellite to position.*/
        double k = (-b - Math.sqrt(det)) / (2 * a);
        vx = _radiusG + k * vx;
        vy *= k;
        vz *= k;

        /* Calculation of longitude and latitude.*/
        double lplam = Math.atan2(vy, vx);
        double lpphi = Math.atan(vz * Math.cos(lplam) / vx);
        lpphi = Math.atan(_radiusPInv2 * Math.tan(lpphi));

        out.x = lplam;
        out.y = lpphi;
    }

    /**
     * Returns true if this projection is equal area
     */
    @Override
    public boolean isEqualArea() {
        return false;
    }

    @Override
    public boolean hasInverse() {
        return true;
    }

    @Override
    public String toString() {
        return "Geostationary Satellite";
    }
}
