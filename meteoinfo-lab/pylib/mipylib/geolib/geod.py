"""
The Geod class can perform forward and inverse geodetic, or
Great Circle, computations.  The forward computation involves
determining latitude, longitude and back azimuth of a terminus
point given the latitude and longitude of an initial point, plus
azimuth and distance. The inverse computation involves
determining the forward and back azimuths and distance given the
latitudes and longitudes of an initial and terminus point.
"""

from org.meteoinfo.projection import GeodeticCalculator

import mipylib.numeric as np

__all__ = ["Geod"]

class Geod(object):
    """
    performs forward and inverse geodetic, or Great Circle,
    computations.  The forward computation (using the 'fwd' method)
    involves determining latitude, longitude and back azimuth of a
    terminus point given the latitude and longitude of an initial
    point, plus azimuth and distance. The inverse computation (using
    the 'inv' method) involves determining the forward and back
    azimuths and distance given the latitudes and longitudes of an
    initial and terminus point.

    Attributes
    ----------
    a: float
        The ellipsoid equatorial radius, or semi-major axis.
    b: float
        The ellipsoid polar radius, or semi-minor axis.
    es: float
        The 'eccentricity' of the ellipse, squared (1-b2/a2).
    f: float
        The ellipsoid 'flattening' parameter ( (a-b)/a ).

    """

    def __init__(self, **kwargs):
        """
        initialize a Geod class instance.
        """
        if 'ellps' in kwargs:
            self._geod = GeodeticCalculator(kwargs['ellps'])
        elif 'semi_major_axis' in kwargs and 'flattening' in kwargs:
            self._geod = GeodeticCalculator(kwargs['semi_major_axis'], kwargs['flattening'])
        else:
            self._geod = GeodeticCalculator()

    def fwd(self, lons, lats, az, dist):
        """
        Forward transformation

        Determine longitudes, latitudes and back azimuths of terminus
        points given longitudes and latitudes of initial points,
        plus forward azimuths and distances.

        Parameters
        ----------
        lons: array, :class:`array`, list, tuple, or scalar
            Longitude(s) of initial point(s)
        lats: array, :class:`array`, list, tuple, or scalar
            Latitude(s) of initial point(s)
        az: array, :class:`array`, list, tuple, or scalar
            Forward azimuth(s)
        dist: array, :class:`array`, list, tuple, or scalar
            Distance(s) between initial and terminus point(s)
            in meters
        radians: bool, default=False
            If True, the input data is assumed to be in radians.
            Otherwise, the data is assumed to be in degrees.

        Returns
        -------
        array, :class:`array`, list, tuple, or scalar:
            Longitude(s) of terminus point(s)
        array, :class:`array`, list, tuple, or scalar:
            Latitude(s) of terminus point(s)
        array, :class:`array`, list, tuple, or scalar:
            Back azimuth(s)
        """
        if isinstance(lons, (list, tuple)):
            lons = np.array(lons)
        if isinstance(lats, (list, tuple)):
            lats = np.array(lats)
        if isinstance(az, (list, tuple)):
            az = np.array(az)
        if isinstance(dist, (list, tuple)):
            dist = np.array(dist)

        if isinstance(lons, np.NDArray):
            r = self._geod.forward(lons._array, lats._array, az._array, dist._array)
            return np.array(r[0]), np.array(r[1]), np.array(r[2])
        else:
            r = self._geod.forward(lons, lats, az, dist)
            return r.lon2, r.lat2, r.azi2

    def inv(self, lons1, lats1, lons2, lats2):
        """
        Inverse transformation

        Determine forward and back azimuths, plus distances
        between initial points and terminus points.

        Parameters
        ----------
        lons1: array, :class:`array`, list, tuple, or scalar
            Longitude(s) of initial point(s)
        lats1: array, :class:`array`, list, tuple, or scalar
            Latitude(s) of initial point(s)
        lons2: array, :class:`array`, list, tuple, or scalar
            Longitude(s) of terminus point(s)
        lats2: array, :class:`array`, list, tuple, or scalar
            Latitude(s) of terminus point(s)
        radians: bool, default=False
            If True, the input data is assumed to be in radians.
            Otherwise, the data is assumed to be in degrees.

        Returns
        -------
        array, :class:`array`, list, tuple, or scalar:
            Forward azimuth(s)
        array, :class:`array`, list, tuple, or scalar:
            Back azimuth(s)
        array, :class:`array`, list, tuple, or scalar:
            Distance(s) between initial and terminus point(s)
            in meters
        """
        if isinstance(lons1, (list, tuple)):
            lons1 = np.array(lons1)
        if isinstance(lats1, (list, tuple)):
            lats1 = np.array(lats1)
        if isinstance(lons2, (list, tuple)):
            lons2 = np.array(lons2)
        if isinstance(lats2, (list, tuple)):
            lats2 = np.array(lats2)

        if isinstance(lons1, np.NDArray):
            r = self._geod.inverse(lons1._array, lats1._array, lons2._array, lats2._array)
            return np.array(r[0]), np.array(r[1]), np.array(r[2])
        else:
            r = self._geod.inverse(lons1, lats1, lons2, lats2)
            return r.azi1, r.azi2, r.s12