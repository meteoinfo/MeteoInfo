/*
 Copyright 2006 Jerry Huxtable

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.meteoinfo.projection.proj4j.proj;

import org.meteoinfo.projection.proj4j.*;
import org.meteoinfo.projection.proj4j.datum.Ellipsoid;
import org.meteoinfo.projection.proj4j.units.AngleFormat;
import org.meteoinfo.projection.proj4j.units.Unit;
import org.meteoinfo.projection.proj4j.units.Units;
import org.meteoinfo.projection.proj4j.util.ProjectionMath;

/**
 * A map projection is a mathematical algorithm for representing a spheroidal
 * surface on a plane. A single projection defines a (usually infinite) family
 * of {@link CoordinateReferenceSystem}s, distinguished by different values for
 * the projection parameters.
 */
public abstract class Projection implements Cloneable {

    /**
     * The minimum latitude of the bounds of this projection
     */
    protected double minLatitude = -Math.PI / 2;
    /**
     * The minimum longitude of the bounds of this projection. This is relative
     * to the projection centre.
     */
    protected double minLongitude = -Math.PI;
    /**
     * The maximum latitude of the bounds of this projection
     */
    protected double maxLatitude = Math.PI / 2;
    /**
     * The maximum longitude of the bounds of this projection. This is relative
     * to the projection centre.
     */
    protected double maxLongitude = Math.PI;
    /**
     * The latitude of the centre of projection
     */
    protected double projectionLatitude = 0.0;
    protected double projectionLatitudeDegrees = 0.0;
    /**
     * The longitude of the centre of projection, in radians
     */
    protected double projectionLongitude = 0.0;
    protected double projectionLongitudeDegrees = 0.0;
    /**
     * Standard parallel 1 (for projections which use it)
     */
    protected double projectionLatitude1 = 0.0;
    protected double projectionLatitude1Degrees = 0.0;
    /**
     * Standard parallel 2 (for projections which use it)
     */
    protected double projectionLatitude2 = 0.0;
    protected double projectionLatitude2Degrees = 0.0;
    /**
     * The projection alpha value
     */
    protected double alpha = Double.NaN;
    /**
     * The projection lonc value
     */
    protected double lonc = Double.NaN;
    /**
     * The projection scale factor
     */
    protected double scaleFactor = 1.0;
    /**
     * The false Easting of this projection
     */
    protected double falseEasting = 0;
    /**
     * The false Northing of this projection
     */
    protected double falseNorthing = 0;
    /**
     * Indicates whether a Southern Hemisphere UTM zone
     */
    protected boolean isSouth = false;
    /**
     * The latitude of true scale. Only used by specific projections.
     */
    protected double trueScaleLatitude = 0.0;
    protected double trueScaleLatitudeDegrees = 0.0;
    /**
     * The equator radius
     */
    protected double a = 0;
    /**
     * The eccentricity
     */
    protected double e = 0;
    /**
     * The eccentricity squared
     */
    protected double es = 0;
    /**
     * 1-(eccentricity squared)
     */
    protected double one_es = 0;
    /**
     * 1/(1-(eccentricity squared))
     */
    protected double rone_es = 0;
    /**
     * The ellipsoid used by this projection
     */
    protected Ellipsoid ellipsoid;
    /**
     * True if this projection is using a sphere (es == 0)
     */
    protected boolean spherical;
    /**
     * True if this projection is geocentric
     */
    protected boolean geocentric;
    /**
     * The name of this projection
     */
    protected String name = null;
    /**
     * Conversion factor from metres to whatever units the projection uses.
     */
    protected double fromMetres = 1;
    /**
     * The total scale factor = Earth radius * units
     */
    protected double totalScale = 0;
    /**
     * falseEasting, adjusted to the appropriate units using fromMetres
     */
    private double totalFalseEasting = 0;
    /**
     * falseNorthing, adjusted to the appropriate units using fromMetres
     */
    private double totalFalseNorthing = 0;
    /**
     * units of this projection. Default is metres, but may be degrees
     */
    protected Unit unit = null;
    /**
     * Height of orbit - Geostationary satellite projection
     */
    protected double heightOfOrbit = 35785831.0;
    protected String proj4Name;
    // Some useful constants
    protected final static double HALFPI = 1.5707963267948966;
    protected final static double EPS10 = 1e-10;
    protected final static double RTD = 180.0 / Math.PI;
    protected final static double DTR = Math.PI / 180.0;

    protected Projection() {
        setEllipsoid(Ellipsoid.SPHERE);
    }

    @Override
    public Object clone() {
        try {
            Projection e = (Projection) super.clone();
            return e;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    /**
     * Projects a geographic point (in degrees), producing a projected result
     * (in the units of the target coordinate system).
     *
     * @param src the input geographic coordinate (in degrees)
     * @param dst the projected coordinate (in coordinate system units)
     * @return the target coordinate
     */
    public ProjCoordinate project(ProjCoordinate src, ProjCoordinate dst) {
        double x = src.x * DTR;
        if (projectionLongitude != 0) {
            x = ProjectionMath.normalizeLongitude(x - projectionLongitude);
        }
        return projectRadians(x, src.y * DTR, dst);
    }

    /**
     * Projects a geographic point (in radians), producing a projected result
     * (in the units of the target coordinate system).
     *
     * @param src the input geographic coordinate (in radians)
     * @param dst the projected coordinate (in coordinate system units)
     * @return the target coordinate
     *
     */
    public ProjCoordinate projectRadians(ProjCoordinate src, ProjCoordinate dst) {
        double x = src.x;
        if (projectionLongitude != 0) {
            x = ProjectionMath.normalizeLongitude(x - projectionLongitude);
        }
        return projectRadians(x, src.y, dst);
    }

    /**
     * Transform a geographic point (in radians), producing a projected result
     * (in the units of the target coordinate system).
     *
     * @param x the geographic x ordinate (in radians)
     * @param y the geographic y ordinate (in radians)
     * @param dst the projected coordinate (in coordinate system units)
     * @return the target coordinate
     */
    private ProjCoordinate projectRadians(double x, double y, ProjCoordinate dst) {
        project(x, y, dst);
        if (unit == Units.DEGREES) {
            // convert radians to DD
            dst.x *= RTD;
            dst.y *= RTD;
        } else {
            // assume result is in metres
            dst.x = totalScale * dst.x + totalFalseEasting;
            dst.y = totalScale * dst.y + totalFalseNorthing;
        }
        return dst;
    }

    /**
     * Computes the projection of a given point (i.e. from geographics to
     * projection space). This should be overridden for all projections.
     *
     * @param x the geographic x ordinate (in radians)
     * @param y the geographic y ordinatee (in radians)
     * @param dst the projected coordinate (in coordinate system units)
     * @return the target coordinate
     */
    protected ProjCoordinate project(double x, double y, ProjCoordinate dst) {
        dst.x = x;
        dst.y = y;
        return dst;
    }

    /**
     * Inverse-projects a point (in the units defined by the coordinate system),
     * producing a geographic result (in degrees)
     *
     * @param src the input projected coordinate (in coordinate system units)
     * @param dst the inverse-projected geographic coordinate (in degrees)
     * @return the target coordinate
     */
    public ProjCoordinate inverseProject(ProjCoordinate src, ProjCoordinate dst) {
        inverseProjectRadians(src, dst);
        dst.x *= RTD;
        dst.y *= RTD;
        return dst;
    }

    /**
     * Inverse-transforms a point (in the units defined by the coordinate
     * system), producing a geographic result (in radians)
     *
     * @param src the input projected coordinate (in coordinate system units)
     * @param dst the inverse-projected geographic coordinate (in radians)
     * @return the target coordinate
     *
     */
    public ProjCoordinate inverseProjectRadians(ProjCoordinate src, ProjCoordinate dst) {
        double x;
        double y;
        if (unit == Units.DEGREES) {
            // convert DD to radians
            x = src.x * DTR;
            y = src.y * DTR;
        } else {
            x = (src.x - totalFalseEasting) / totalScale;
            y = (src.y - totalFalseNorthing) / totalScale;
        }
        projectInverse(x, y, dst);
        if (dst.x < -Math.PI) {
            dst.x = -Math.PI;
        } else if (dst.x > Math.PI) {
            dst.x = Math.PI;
        }
        if (projectionLongitude != 0) {
            dst.x = ProjectionMath.normalizeLongitude(dst.x + projectionLongitude);
        }
        return dst;
    }

    /**
     * Computes the inverse projection of a given point (i.e. from projection
     * space to geographics). This should be overridden for all projections.
     *
     * @param x the projected x ordinate (in coordinate system units)
     * @param y the projected y ordinate (in coordinate system units)
     * @param dst the inverse-projected geographic coordinate (in radians)
     * @return the target coordinate
     */
    protected ProjCoordinate projectInverse(double x, double y, ProjCoordinate dst) {
        dst.x = x;
        dst.y = y;
        return dst;
    }

    /**
     * Tests whether this projection is conformal. A conformal projection
     * preserves local angles.
     *
     * @return true if this projection is conformal
     */
    public boolean isConformal() {
        return false;
    }

    /**
     * Tests whether this projection is equal-area An equal-area projection
     * preserves relative sizes of projected areas.
     *
     * @return true if this projection is equal-area
     */
    public boolean isEqualArea() {
        return false;
    }

    /**
     * Tests whether this projection has an inverse. If this method returns
     * <tt>true</tt>
     * then the {@link #inverseProject(ProjCoordinate, ProjCoordinate)} and
     * {@link #inverseProjectRadians(ProjCoordinate, ProjCoordinate)} methods
     * will return meaningful results.
     *
     * @return true if this projection has an inverse
     */
    public boolean hasInverse() {
        return false;
    }

    /**
     * Tests whether under this projection lines of latitude and longitude form
     * a rectangular grid
     */
    public boolean isRectilinear() {
        return false;
    }

    /**
     * Returns true if latitude lines are parallel for this projection
     */
    public boolean parallelsAreParallel() {
        return isRectilinear();
    }

    /**
     * Returns true if the given lat/long point is visible in this projection
     */
    public boolean inside(double x, double y) {
        x = normalizeLongitude((float) (x * DTR - projectionLongitude));
        return minLongitude <= x && x <= maxLongitude && minLatitude <= y && y <= maxLatitude;
    }

    /**
     * Set the name of this projection.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get projection name
     * @return Projection name
     */
    public String getName() {
        if (name != null) {
            return name;
        }
        return toString();
    }
    
    /**
     * Get proj4 projection name
     * @return Proj4 projection name
     */
    public String getProj4Name(){
        return proj4Name;
    }

    /**
     * Get a string which describes this projection in PROJ.4 format.
     */
    public String getPROJ4Description() {
        AngleFormat format = new AngleFormat(AngleFormat.ddmmssPattern, false);
        StringBuffer sb = new StringBuffer();
        sb.append(
                "+proj=" + getName()
                + " +a=" + a);
        if (es != 0) {
            sb.append(" +es=" + es);
        }
        sb.append(" +lon_0=");
        format.format(projectionLongitude, sb, null);
        sb.append(" +lat_0=");
        format.format(projectionLatitude, sb, null);
        if (falseEasting != 1) {
            sb.append(" +x_0=" + falseEasting);
        }
        if (falseNorthing != 1) {
            sb.append(" +y_0=" + falseNorthing);
        }
        if (scaleFactor != 1) {
            sb.append(" +k=" + scaleFactor);
        }
        if (fromMetres != 1) {
            sb.append(" +fr_meters=" + fromMetres);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "None";
    }

    /**
     * Set the minimum latitude. This is only used for Shape clipping and
     * doesn't affect projection.
     */
    public void setMinLatitude(double minLatitude) {
        this.minLatitude = minLatitude;
    }

    public double getMinLatitude() {
        return minLatitude;
    }

    /**
     * Set the maximum latitude. This is only used for Shape clipping and
     * doesn't affect projection.
     */
    public void setMaxLatitude(double maxLatitude) {
        this.maxLatitude = maxLatitude;
    }

    public double getMaxLatitude() {
        return maxLatitude;
    }

    public double getMaxLatitudeDegrees() {
        return maxLatitude * RTD;
    }

    public double getMinLatitudeDegrees() {
        return minLatitude * RTD;
    }

    public void setMinLongitude(double minLongitude) {
        this.minLongitude = minLongitude;
    }

    public double getMinLongitude() {
        return minLongitude;
    }

    public void setMinLongitudeDegrees(double minLongitude) {
        this.minLongitude = DTR * minLongitude;
    }

    public double getMinLongitudeDegrees() {
        return minLongitude * RTD;
    }

    public void setMaxLongitude(double maxLongitude) {
        this.maxLongitude = maxLongitude;
    }

    public double getMaxLongitude() {
        return maxLongitude;
    }

    public void setMaxLongitudeDegrees(double maxLongitude) {
        this.maxLongitude = DTR * maxLongitude;
    }

    public double getMaxLongitudeDegrees() {
        return maxLongitude * RTD;
    }

    /**
     * Set the projection latitude in radians.
     */
    public void setProjectionLatitude(double projectionLatitude) {
        this.projectionLatitude = projectionLatitude;
        this.projectionLatitudeDegrees = projectionLatitude * RTD;
    }

    public double getProjectionLatitude() {
        return projectionLatitude;
    }

    /**
     * Set the projection latitude in degrees.
     */
    public void setProjectionLatitudeDegrees(double projectionLatitude) {
        this.projectionLatitudeDegrees = projectionLatitude;
        this.projectionLatitude = DTR * projectionLatitude;
    }

    public double getProjectionLatitudeDegrees() {
        //return projectionLatitude * RTD;
        return projectionLatitudeDegrees;
    }

    /**
     * Set the projection longitude in radians.
     */
    public void setProjectionLongitude(double projectionLongitude) {
        this.projectionLongitude = normalizeLongitudeRadians(projectionLongitude);
        this.projectionLongitudeDegrees = this.projectionLongitude * RTD;
    }

    public double getProjectionLongitude() {
        return projectionLongitude;
    }

    /**
     * Set the projection longitude in degrees.
     */
    public void setProjectionLongitudeDegrees(double projectionLongitude) {
        this.projectionLongitude = DTR * projectionLongitude;
        this.projectionLongitudeDegrees = projectionLongitude;
    }

    public double getProjectionLongitudeDegrees() {
        //return projectionLongitude * RTD;
        return this.projectionLongitudeDegrees;
    }

    /**
     * Set the latitude of true scale in radians. This is only used by certain
     * projections.
     */
    public void setTrueScaleLatitude(double trueScaleLatitude) {
        this.trueScaleLatitude = trueScaleLatitude;
        this.trueScaleLatitudeDegrees = trueScaleLatitude * RTD;
    }

    public double getTrueScaleLatitude() {
        return trueScaleLatitude;
    }

    /**
     * Set the latitude of true scale in degrees. This is only used by certain
     * projections.
     */
    public void setTrueScaleLatitudeDegrees(double trueScaleLatitude) {
        this.trueScaleLatitude = DTR * trueScaleLatitude;
        this.trueScaleLatitudeDegrees = trueScaleLatitude;
    }

    public double getTrueScaleLatitudeDegrees() {
        //return trueScaleLatitude * RTD;
        return this.trueScaleLatitudeDegrees;
    }

    /**
     * Set the projection latitude in radians.
     */
    public void setProjectionLatitude1(double projectionLatitude1) {
        this.projectionLatitude1 = projectionLatitude1;
        this.projectionLatitude1Degrees = projectionLatitude1 * RTD;
    }

    public double getProjectionLatitude1() {
        return projectionLatitude1;
    }

    /**
     * Set the projection latitude in degrees.
     */
    public void setProjectionLatitude1Degrees(double projectionLatitude1) {
        this.projectionLatitude1 = DTR * projectionLatitude1;
        this.projectionLatitude1Degrees = projectionLatitude1;
    }

    public double getProjectionLatitude1Degrees() {
        //return projectionLatitude1 * RTD;
        return this.projectionLatitude1Degrees;
    }

    /**
     * Set the projection latitude in radians.
     */
    public void setProjectionLatitude2(double projectionLatitude2) {
        this.projectionLatitude2 = projectionLatitude2;
        this.projectionLatitude2Degrees = projectionLatitude2 * RTD;
    }

    public double getProjectionLatitude2() {
        return projectionLatitude2;
    }

    /**
     * Set the projection latitude in degrees.
     */
    public void setProjectionLatitude2Degrees(double projectionLatitude2) {
        this.projectionLatitude2 = DTR * projectionLatitude2;
        this.projectionLatitude2Degrees = projectionLatitude2;
    }

    public double getProjectionLatitude2Degrees() {
        //return projectionLatitude2 * RTD;
        return this.projectionLatitude2Degrees;
    }

    /**
     * Sets the alpha value.
     */
    public void setAlphaDegrees(double alpha) {
        this.alpha = DTR * alpha;
    }

    /**
     * Gets the alpha value, in radians.
     *
     * @return the alpha value
     */
    public double getAlpha() {
        return alpha;
    }

    /**
     * Sets the lonc value.
     */
    public void setLonCDegrees(double lonc) {
        this.lonc = DTR * lonc;
    }

    /**
     * Gets the lonc value, in radians.
     *
     * @return the lonc value
     */
    public double getLonC() {
        return lonc;
    }

    /**
     * Set the false Northing in projected units.
     */
    public void setFalseNorthing(double falseNorthing) {
        this.falseNorthing = falseNorthing;
    }

    public double getFalseNorthing() {
        return falseNorthing;
    }

    /**
     * Set the false Easting in projected units.
     */
    public void setFalseEasting(double falseEasting) {
        this.falseEasting = falseEasting;
    }

    public double getFalseEasting() {
        return falseEasting;
    }

    public void setSouthernHemisphere(boolean isSouth) {
        this.isSouth = isSouth;
    }

    public boolean getSouthernHemisphere() {
        return isSouth;
    }

    /**
     * Set the projection scale factor. This is set to 1 by default. This value
     * is called "k0" in PROJ.4.
     * @param scaleFactor Scale factor
     */
    public void setScaleFactor(double scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    /**
     * Gets the projection scale factor. This value is called "k0" in PROJ.4.
     *
     * @return Projection scale factor
     */
    public double getScaleFactor() {
        return scaleFactor;
    }

    public double getEquatorRadius() {
        return a;
    }

    /**
     * Set the conversion factor from metres to projected units. This is set to
     * 1 by default.
     */
    public void setFromMetres(double fromMetres) {
        this.fromMetres = fromMetres;
    }

    public double getFromMetres() {
        return fromMetres;
    }

    public void setEllipsoid(Ellipsoid ellipsoid) {
        this.ellipsoid = ellipsoid;
        a = ellipsoid.equatorRadius;
        e = ellipsoid.eccentricity;
        es = ellipsoid.eccentricity2;
    }

    public Ellipsoid getEllipsoid() {
        return ellipsoid;
    }

    /**
     * Returns the ESPG code for this projection, or 0 if unknown.
     */
    public int getEPSGCode() {
        return 0;
    }

    public void setUnits(Unit unit) {
        this.unit = unit;
    }
    
    /**
     * Get height of orbit - Geostationary satellite projection
     * @return Height of orbit
     */
    public double getHeightOfOrbit(){
        return this.heightOfOrbit;
    }
    
    /**
     * Set height of orbit - Geostationary satellite projection
     * @param h Height of orbit
     */
    public void setHeightOfOrbit(double h){
        this.heightOfOrbit = h;
    }

    /**
     * Initialize the projection. This should be called after setting parameters
     * and before using the projection. This is for performance reasons as
     * initialization may be expensive.
     */
    public void initialize() {
        spherical = (e == 0.0);
        one_es = 1 - es;
        rone_es = 1.0 / one_es;
        totalScale = a * fromMetres;
        totalFalseEasting = falseEasting * fromMetres;
        totalFalseNorthing = falseNorthing * fromMetres;
    }

    public static float normalizeLongitude(float angle) {
        if (Double.isInfinite(angle) || Double.isNaN(angle)) {
            throw new InvalidValueException("Infinite or NaN longitude");
        }
        while (angle > 180) {
            angle -= 360;
        }
        while (angle < -180) {
            angle += 360;
        }
        return angle;
    }

    public static double normalizeLongitudeRadians(double angle) {
        if (Double.isInfinite(angle) || Double.isNaN(angle)) {
            throw new InvalidValueException("Infinite or NaN longitude");
        }
        while (angle > Math.PI) {
            angle -= ProjectionMath.TWOPI;
        }
        while (angle < -Math.PI) {
            angle += ProjectionMath.TWOPI;
        }
        return angle;
    }
    
    /**
     * Check if this projection is equal with other projection
     * @param proj The other projection
     * @return Boolean
     */
    public boolean isEqual(Projection proj){
        if (!this.proj4Name.equals(proj.proj4Name))
            return false;
        if (this.a != proj.a)
            return false;        
        if (this.e != proj.e)
            return false;
        if (!this.ellipsoid.isEqual(proj.ellipsoid, 0.0000001))
            return false;
        if (this.es != proj.es)
            return false;
        if (this.falseEasting != proj.falseEasting)
            return false;
        if (this.falseNorthing != proj.falseNorthing)
            return false;
        if (this.fromMetres != proj.fromMetres)
            return false;
        if (this.heightOfOrbit != proj.heightOfOrbit)
            return false;
        if (this.projectionLatitudeDegrees != proj.projectionLatitudeDegrees)
            return false;
        if (this.projectionLatitude1Degrees != proj.projectionLatitude1Degrees)
            return false;
        if (this.projectionLatitude2Degrees != proj.projectionLatitude2Degrees)
            return false;
        if (this.projectionLongitudeDegrees != proj.projectionLongitudeDegrees)
            return false;
        if (this.scaleFactor != proj.scaleFactor)
            return false;
        if (this.trueScaleLatitudeDegrees != proj.trueScaleLatitudeDegrees)
            return false;
        
        return true;        
    }
        
}
