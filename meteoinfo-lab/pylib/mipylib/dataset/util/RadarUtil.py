from mipylib import geolib
import mipylib.numeric as np
from org.meteoinfo.data.meteodata.radar import Transform


__all__ = ['antenna_to_cartesian','antenna_to_geographic','get_aeqd_projection',
           'geographic_to_cartesian','cartesian_to_geographic']


def antenna_to_cartesian(distance, azimuth, elevation, h=None):
    """
    Convert antenna coordinate to cartesian coordinate.

    :param distance: (*array*) Distances to the center of the radar gates (bins) in meters.
    :param azimuth: (*array*) Azimuth angle of the radar in degrees.
    :param elevation: (*array*) Elevation angle of the radar in degrees.
    :param h: (*float*) Altitude of the instrument, above sea level, units:m.

    :return: Cartesian coordinate in meters from the radar (x, y, z).
    """
    azimuth = np.deg2rad(azimuth)
    elevation = np.deg2rad(elevation)
    if azimuth.ndim == 1:
        nd = distance.shape[0]
        na = azimuth.shape[0]
        distance, azimuth = np.meshgrid(distance, azimuth)
        if isinstance(elevation, np.NDArray):
            elevation = elevation.reshape(na, 1)
            elevation = elevation.repeat(nd, axis=1)
            elevation = elevation._array
        if h is None:
            r = Transform.antennaToCartesian(distance._array, azimuth._array, elevation)
        else:
            r = Transform.antennaToCartesian(distance._array, azimuth._array, elevation, h)

        return np.array(r[0]), np.array(r[1]), np.array(r[2])
    else:
        nd = distance.shape[0]
        ns, na = azimuth.shape
        x = np.empty((ns, na, nd))
        y = np.empty((ns, na, nd))
        z = np.empty((ns,na,nd))
        for i in range(ns):
            dis, azi = np.meshgrid(distance, azimuth[i])
            if elevation.ndim == 1:
                ele = elevation[i]
            else:
                ele = elevation[i].reshape(na, 1).copy()
                ele = ele.repeat(nd, axis=1)
                ele = ele._array
            if h is None:
                r = Transform.antennaToCartesian(dis._array, azi._array, ele)
            else:
                r = Transform.antennaToCartesian(dis._array, azi._array, ele, h)
            x[i] = np.array(r[0])
            y[i] = np.array(r[1])
            z[i] = np.array(r[2])

        return x, y, z

def antenna_to_geographic(rlon, rlat, distance, azimuth, elevation, h=None):
    """
    Convert antenna coordinate to geographic (longitude/latitude) coordinate.

    :param rlon: (*float*) Longitude of the radar.
    :param rlat: (*float*) Latitude of the radar.
    :param distance: (*array*) Distances to the center of the radar gates (bins) in meters.
    :param azimuth: (*array*) Azimuth angle of the radar in degrees.
    :param elevation: (*array*) Elevation angle of the radar in degrees.
    :param h: (*float*) Altitude of the instrument, above sea level, units:m.

    :return: Geographic coordinate.
    """
    x, y, z = antenna_to_cartesian(distance, azimuth, elevation, h)
    lon, lat = cartesian_to_geographic(rlon, rlat, x, y)

    return lon, lat, z

def get_aeqd_projection(rlon, rlat):
    """
    Get azimuth equidistant projection.

    :param rlon: (*float*) Radar longitude.
    :param rlat: (*float*) Radar latitude.

    :return: Azimuth equidistant projection.
    """
    return geolib.projinfo(proj='aeqd', lon_0=rlon, lat_0=rlat)

def geographic_to_cartesian(rlon, rlat, lon, lat):
    """
    Convert cartesian coordinates to geographic coordinates.

    :param rlon: (*float*) Radar longitude.
    :param rlat: (*float*) Radar latitude.
    :param lon: (*array*) Longitude coordinates in degrees.
    :param lat: (*array*) Latitude coordinates in degrees.

    :return: Cartesian coordinates in meters from the radar (x, y).
    """
    proj = get_aeqd_projection(rlon, rlat)
    return geolib.project(lon, lat, toproj=proj)

def cartesian_to_geographic(rlon, rlat, x, y):
    """
    Convert geographic coordinates to cartesian coordinates.

    :param rlon: (*float*) Radar longitude.
    :param rlat: (*float*) Radar latitude.
    :param x: (*array*) X coordinates in meters.
    :param y: (*array*) Y coordinates in meters.

    :return: Cartesian coordinates in meters from the radar (x, y).
    """
    proj = get_aeqd_projection(rlon, rlat)
    return geolib.project(x, y, fromproj=proj)
