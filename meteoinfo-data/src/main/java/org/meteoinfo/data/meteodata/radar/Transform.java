package org.meteoinfo.data.meteodata.radar;

import org.meteoinfo.ndarray.math.ArrayMath;

public class Transform {

    static double R = 6371.0 * 1000.0 * 4.0 / 3.0;     // effective radius of earth in meters.

    /**
     * Convert antenna coordinate to cartesian coordinate
     * @param r Distances to the center of the radar gates (bins) in meters
     * @param a Azimuth angle of the radar in radians
     * @param e Elevation angle of the radar in radians
     * @return Cartesian coordinate in meters from the radar
     */
    public static double[] antennaToCartesian(float r, float a, float e) {
        double z = Math.pow(r * r + R * R + 2.0 * r * R * Math.sin(e), 0.5) - R;
        double s = R * Math.asin(r * Math.cos(e) / (R + z));  // arc length in m.
        double x = s * Math.sin(a);
        double y = s * Math.cos(a);

        return new double[]{x, y, z};
    }

    /**
     * Convert antenna coordinate to cartesian coordinate
     * @param r Distances to the center of the radar gates (bins) in meters
     * @param a Azimuth angle of the radar in radians
     * @param e Elevation angle of the radar in radians
     * @param h Altitude of the instrument, above sea level, units:m
     * @return Cartesian coordinate in meters from the radar
     */
    public static double[] antennaToCartesian(float r, float a, float e, float h) {
        double R = 6371.0 * 1000.0 * 4.0 / 3.0;     // effective radius of earth in meters.

        double z = Math.pow(Math.pow(r * Math.cos(e), 2) + Math.pow(R + h + r * Math.sin(e), 2), 0.5) - R;
        double s = R * Math.asin(r * Math.cos(e) / (R + z));  // arc length in m.
        double x = s * Math.sin(a);
        double y = s * Math.cos(a);

        return new double[]{x, y, z};
    }

    private static double azimuth(float x, float y) {
        double az = Math.PI / 2 - Math.atan2(x + y, 1);
        if (az < 0) {
            az = 2 * Math.PI + az;
        }
        return Math.toDegrees(az);
    }

    /**
     * Convert cartesian coordinate to antenna coordinate
     * @param x x coordinate in meters
     * @param y y coordinate in meters
     * @param z z coordinate in meters
     * @param h Altitude of the instrument, above sea level, units:m
     * @return Antenna coordinate from the radar
     */
    public static double[] cartesianToAntenna(float x, float y, float z, float h) {
        double ranges = Math.sqrt(Math.pow(R + h, 2) + Math.pow(R + z, 2) - 2 * (R + h) * (R + z) *
                Math.cos(Math.sqrt(x * x + y * y)  / R));
        double elevation = Math.acos((R + z) * Math.sin(Math.sqrt(x * x + y * y) / R) / ranges) *
                180. / Math.PI;
        double azimuth = azimuth(x, y);

        return new double[]{azimuth, ranges, elevation};
    }
}
