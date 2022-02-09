package org.meteoinfo.projection;

import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;
import org.locationtech.proj4j.datum.Ellipsoid;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.IndexIterator;

public class GeodeticCalculator {
    /** The encapsulated ellipsoid. */
    private final Ellipsoid ellipsoid;

    /*
     * The semi major axis of the reference ellipsoid.
     */
    private final double semiMajorAxis;

    /*
     * The flattening the reference ellipsoid.
     */
    private final double flattening;

    /** The object that carries out the geodesic calculations. */
    private Geodesic geod;

    /**
     * Constructor
     */
    public GeodeticCalculator() {
        this(Ellipsoid.WGS84);
    }

    /**
     * Constructor
     * @param ellipsoid
     */
    public GeodeticCalculator(Ellipsoid ellipsoid) {
        this.ellipsoid = ellipsoid;
        this.semiMajorAxis = ellipsoid.getA();
        this.flattening = (ellipsoid.getA() - ellipsoid.getB()) / ellipsoid.getA();
        this.geod = new Geodesic(semiMajorAxis, flattening);
    }

    /**
     * Constructor
     * @param ellipsoidName Ellipsoid name
     */
    public GeodeticCalculator(String ellipsoidName) {
        Ellipsoid ellipsoid = Ellipsoid.WGS84;
        for (Ellipsoid e : Ellipsoid.ellipsoids) {
            if (e.shortName.equalsIgnoreCase(ellipsoidName)) {
                ellipsoid = e;
                break;
            }
        }
        this.ellipsoid = ellipsoid;
        this.semiMajorAxis = ellipsoid.getA();
        this.flattening = (ellipsoid.getA() - ellipsoid.getB()) / ellipsoid.getA();
        this.geod = new Geodesic(semiMajorAxis, flattening);
    }

    /**
     * Constructor
     * @param semiMajorAxis
     * @param flattening
     */
    public GeodeticCalculator(double semiMajorAxis, double flattening) {
        double e2 = 2* flattening - flattening * flattening;
        this.ellipsoid = new Ellipsoid("custom", semiMajorAxis, e2, "custom");
        this.semiMajorAxis = semiMajorAxis;
        this.flattening = flattening;
        this.geod = new Geodesic(semiMajorAxis, flattening);
    }

    /**
     * Get ellipsoid
     * @return Ellipsoid
     */
    public Ellipsoid getEllipsoid() {
        return this.ellipsoid;
    }

    /**
     * Forward transform
     * @param lon Longitude of initial point
     * @param lat Latitude of initial point
     * @param azimuth forward azimuth
     * @param distance Distance between initial and terminus points
     * @return Geodesic data
     */
    public GeodesicData forward(double lon, double lat, double azimuth, double distance) {
        return this.geod.Direct(lat, lon, azimuth, distance);
    }

    /**
     * Forward transform
     * @param lons Longitude array of initial points
     * @param lats Latitude array of initial points
     * @param azimuths Forward azimuth array
     * @param distances Distance array between initial and terminus points
     * @return Longitude, latitude array of terminus points and back azimuth array
     */
    public Array[] forward(Array lons, Array lats, Array azimuths, Array distances) {
        lons = lons.copyIfView();
        lats = lats.copyIfView();
        azimuths = azimuths.copyIfView();
        distances = distances.copyIfView();

        Array lons2 = Array.factory(lons.getDataType(), lons.getShape());
        Array lats2 = Array.factory(lons.getDataType(), lons.getShape());
        Array azimuths2 = Array.factory(lons.getDataType(), lons.getShape());
        for (int i = 0; i < lons.getSize(); i++) {
            GeodesicData geodesicData = forward(lons.getDouble(i), lats.getDouble(i), azimuths.getDouble(i),
                    distances.getDouble(i));
            lons2.setDouble(i, geodesicData.lon2);
            lats2.setDouble(i, geodesicData.lat2);
            azimuths2.setDouble(i, geodesicData.azi2);
        }

        return new Array[]{lons2, lats2, azimuths2};
    }

    /**
     * Inverse transform
     * @param lon1 Longitude of initial point
     * @param lat1 Latitude of initial point
     * @param lon2 Longitude of terminus point
     * @param lat2 Latitude of terminus point
     * @return Geodesic data
     */
    public GeodesicData inverse(double lon1, double lat1, double lon2, double lat2) {
        return this.geod.Inverse(lat1, lon1, lat2, lon2);
    }

    /**
     * Inverse transform
     * @param lons1 Longitude array of initial points
     * @param lats1 Latitude array of initial points
     * @param lons2 Longitude array of terminus points
     * @param lats2 Latitude array of terminus points
     * @return Forward and back azimuth array and distance array
     */
    public Array[] inverse(Array lons1, Array lats1, Array lons2, Array lats2) {
        lons1 = lons1.copyIfView();
        lats1 = lats1.copyIfView();
        lons2 = lons2.copyIfView();
        lats2 = lats2.copyIfView();

        Array azimuths1 = Array.factory(lons1.getDataType(), lons1.getShape());
        Array azimuths2 = Array.factory(lons1.getDataType(), lons1.getShape());
        Array distances = Array.factory(lons1.getDataType(), lons1.getShape());
        for (int i = 0; i < lons1.getSize(); i++) {
            GeodesicData geodesicData = inverse(lons1.getDouble(i), lats1.getDouble(i), lons2.getDouble(i),
                    lats2.getDouble(i));
            azimuths1.setDouble(i, geodesicData.azi1);
            azimuths2.setDouble(i, geodesicData.azi2);
            distances.setDouble(i, geodesicData.s12);
        }

        return new Array[]{azimuths1, azimuths2, distances};
    }
}
