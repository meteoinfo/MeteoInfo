from org.meteoinfo.projection import ProjectionInfo
from org.meteoinfo.projection.info import LongLat, Albers, LambertConformalConic, LambertEqualAreaConic, \
    GeostationarySatellite, OrthographicAzimuthal, StereographicAzimuthal
from org.meteoinfo.projection.info import TransverseMercator as JTransverseMercator
from org.meteoinfo.projection.info import Mercator as JMercator
from org.meteoinfo.projection.info import AzimuthEquidistant as JAzimuthEquidistant
from org.meteoinfo.projection.info import EquidistantConic as JEquidistantConic
from org.meteoinfo.projection.info import Hammer as JHammer
from org.meteoinfo.projection.info import LambertAzimuthalEqualArea as JLambertAzimuthalEqualArea
from org.meteoinfo.projection.info import Mollweide as JMollweide
from org.meteoinfo.projection.info import Robinson as JRobinson
from org.meteoinfo.projection.info import Sinusoidal as JSinusoidal
from org.meteoinfo.projection.info import Wagner3 as JWagner3
from org.meteoinfo.projection.info import Airy as JAiry
from org.meteoinfo.projection.info import Aitoff as JAitoff
from org.meteoinfo.projection.info import August as JAugust
from org.locationtech.proj4j import CRSFactory


__all__ = ['AlbersEqualArea','Airy','Aitoff','August','AzimuthalEquidistant','EquidistantConic',
           'Geostationary','Hammer','LambertAzimuthalEqualArea','LambertConformal','LambertEqualArea',
           'Mercator','Mollweide','NorthPolarStereo','Orthographic',
           'PlateCarree','Robinson','Sinusoidal','SouthPolarStereo','Stereographic',
           'TransverseMercator','Wagner3']


crs_factory = CRSFactory()


class PlateCarree(LongLat):

    def __init__(self, central_longitude=0.0):
        proj4_params = ['+proj=longlat', '+lon_0=' + str(central_longitude)]
        crs = crs_factory.createFromParameters('custom', proj4_params)
        LongLat.__init__(self, crs)


class TransverseMercator(JTransverseMercator):
    """
    A Transverse Mercator projection.
    """

    def __init__(self, central_longitude=0.0, central_latitude=0.0,
                 false_easting=0.0, false_northing=0.0, scale_factor=1.0):
        """
        Parameters
        ----------
        central_longitude: optional
            The true longitude of the central meridian in degrees.
            Defaults to 0.
        central_latitude: optional
            The true latitude of the planar origin in degrees. Defaults to 0.
        false_easting: optional
            X offset from the planar origin in metres. Defaults to 0.
        false_northing: optional
            Y offset from the planar origin in metres. Defaults to 0.
        scale_factor: optional
            Scale factor at the central meridian. Defaults to 1.
        """
        proj4_params = ['+proj=tmerc',
                        '+lon_0=' + str(central_longitude),
                        '+lat_0=' + str(central_latitude),
                        '+x_0=' + str(false_easting),
                        '+y_0=' + str(false_northing),
                        '+k=' + str(scale_factor)]
        crs = crs_factory.createFromParameters('custom', proj4_params)
        JTransverseMercator.__init__(self, crs)


class Mercator(JMercator):
    """
    A Transverse Mercator projection.
    """

    def __init__(self, central_longitude=0.0, latitude_true_scale=None,
                 false_easting=0.0, false_northing=0.0, scale_factor=1.0):
        """
        Parameters
        ----------
        central_longitude: optional
            The true longitude of the central meridian in degrees.
            Defaults to 0.
        latitude_true_scale: optional
            The latitude where the scale is 1. Defaults to 0 degrees.
        false_easting: optional
            X offset from the planar origin in metres. Defaults to 0.
        false_northing: optional
            Y offset from the planar origin in metres. Defaults to 0.
        scale_factor: optional
            Scale factor at the central meridian. Defaults to 1.
        """
        proj4_params = ['+proj=merc',
                        '+lon_0=' + str(central_longitude),
                        '+x_0=' + str(false_easting),
                        '+y_0=' + str(false_northing)]

        # If it's None, we don't pass it to Proj4, in which case its default
        # of 0.0 will be used.
        if latitude_true_scale is not None:
            proj4_params.append('+lat_ts=' + str(latitude_true_scale))

        if scale_factor is not None:
            if latitude_true_scale is not None:
                raise ValueError('It does not make sense to provide both '
                                 '"scale_factor" and "latitude_true_scale". ')
            else:
                proj4_params.append('+k_0=' + str(scale_factor))

        crs = crs_factory.createFromParameters('custom', proj4_params)
        JMercator.__init__(self, crs)


class AlbersEqualArea(Albers):
    """
    An Albers Equal Area projection.

    This projection is conic and equal-area.
    """

    def __init__(self, central_longitude=0.0, central_latitude=0.0,
                 false_easting=0.0, false_northing=0.0,
                 standard_parallels=(20.0, 50.0)):
        """
        Parameters
        ----------
        central_longitude: optional
            The central longitude. Defaults to 0.
        central_latitude: optional
            The central latitude. Defaults to 0.
        false_easting: optional
            X offset from planar origin in metres. Defaults to 0.
        false_northing: optional
            Y offset from planar origin in metres. Defaults to 0.
        standard_parallels: optional
            The one or two latitudes of correct scale. Defaults to (20, 50).
        """
        proj4_params = ['+proj=' + 'aea',
                        '+lon_0=' + str(central_longitude),
                        '+lat_0=' + str(central_latitude),
                        '+x_0=' + str(false_easting),
                        '+y_0=' + str(false_northing)]

        if standard_parallels is not None:
            try:
                proj4_params.append('+lat_1=' + str(standard_parallels[0]))
                try:
                    proj4_params.append('+lat_2=' + str(standard_parallels[1]))
                except IndexError:
                    pass
            except TypeError:
                proj4_params.append('+lat_1=' + str(standard_parallels))

        crs = crs_factory.createFromParameters('custom', proj4_params)
        Albers.__init__(self, crs)


class AzimuthalEquidistant(JAzimuthEquidistant):
    """
    An Azimuthal Equidistant projection

    This projection provides accurate angles about and distances through the
    central position. Other angles, distances, or areas may be distorted.
    """

    def __init__(self, central_longitude=0.0, central_latitude=0.0,
                 false_easting=0.0, false_northing=0.0):
        """
        Parameters
        ----------
        central_longitude: optional
            The true longitude of the central meridian in degrees.
            Defaults to 0.
        central_latitude: optional
            The true latitude of the planar origin in degrees.
            Defaults to 0.
        false_easting: optional
            X offset from the planar origin in metres. Defaults to 0.
        false_northing: optional
            Y offset from the planar origin in metres. Defaults to 0.
        """
        proj4_params = ['+proj=' + 'aeqd',
                        '+lon_0=' + str(central_longitude),
                        '+lat_0=' + str(central_latitude),
                        '+x_0=' + str(false_easting),
                        '+y_0=' + str(false_northing)]

        crs = crs_factory.createFromParameters('custom', proj4_params)
        JAzimuthEquidistant.__init__(self, crs)


class EquidistantConic(JEquidistantConic):
    """
    An Equidistant Conic projection.

    This projection is conic and equidistant, and the scale is true along all
    meridians and along one or two specified standard parallels.
    """

    def __init__(self, central_longitude=0.0, central_latitude=0.0,
                 false_easting=0.0, false_northing=0.0,
                 standard_parallels=(20.0, 50.0)):
        """
        Parameters
        ----------
        central_longitude: optional
            The central longitude. Defaults to 0.
        central_latitude: optional
            The true latitude of the planar origin in degrees. Defaults to 0.
        false_easting: optional
            X offset from planar origin in metres. Defaults to 0.
        false_northing: optional
            Y offset from planar origin in metres. Defaults to 0.
        standard_parallels: optional
            The one or two latitudes of correct scale. Defaults to (20, 50).
        """
        proj4_params = ['+proj=' + 'eqdc',
                        '+lon_0=' + str(central_longitude),
                        '+lat_0=' + str(central_latitude),
                        '+x_0=' + str(false_easting),
                        '+y_0=' + str(false_northing)]

        if standard_parallels is not None:
            try:
                proj4_params.append('+lat_1=', str(standard_parallels[0]))
                try:
                    proj4_params.append('+lat_2=' + str(standard_parallels[1]))
                except IndexError:
                    pass
            except TypeError:
                proj4_params.append('+lat_1=' + str(standard_parallels))

        crs = crs_factory.createFromParameters('custom', proj4_params)
        JEquidistantConic.__init__(self, crs)


class Geostationary(GeostationarySatellite):
    """
    A view appropriate for satellites in Geostationary Earth orbit.

    Perspective view looking directly down from above a point on the equator.

    In this projection, the projected coordinates are scanning angles measured
    from the satellite looking directly downward, multiplied by the height of
    the satellite.
    """

    def __init__(self, central_longitude=0.0, satellite_height=35785831,
                 false_easting=0, false_northing=0, semi_major_axis=None,
                 semi_minor_axis=None):
        """
        Parameters
        ----------
        central_longitude: float, optional
            The central longitude. Defaults to 0.
        satellite_height: float, optional
            The height of the satellite. Defaults to 35785831 metres
            (true geostationary orbit).
        false_easting:
            X offset from planar origin in metres. Defaults to 0.
        false_northing:
            Y offset from planar origin in metres. Defaults to 0.
        semi_major_axis:
            Semi-major axis of the ellipsoid, `a`.
        semi_minor_axis:
            Semi-minor axis of the ellipsoid, `b`.
        """
        proj4_params = ['+proj=' + 'geos',
                        '+lon_0=' + str(central_longitude),
                        '+h=' + str(satellite_height),
                        '+x_0=' + str(false_easting),
                        '+y_0=' + str(false_northing)]

        if semi_major_axis is not None:
            proj4_params.append('+a=' + str(semi_major_axis))

        if semi_minor_axis is not None:
            proj4_params.append('+b=' + str(semi_minor_axis))

        crs = crs_factory.createFromParameters('custom', proj4_params)
        GeostationarySatellite.__init__(self, crs)


class Hammer(JHammer):
    """
    A Hammer projection.

    This projection is a modified `.LambertAzimuthalEqualArea` projection,
    similar to `.Aitoff`, and intended to reduce distortion in the outer
    meridians compared to `.Mollweide`. There are no standard lines and only
    the central point is free of distortion.

    """

    _handles_ellipses = False

    def __init__(self, central_longitude=0, false_easting=None,
                 false_northing=None):
        """
        Parameters
        ----------
        central_longitude: float, optional
            The central longitude. Defaults to 0.
        false_easting: float, optional
            X offset from planar origin in metres. Defaults to 0.
        false_northing: float, optional
            Y offset from planar origin in metres. Defaults to 0.

            .. note::
                This projection does not handle elliptical globes.

        """
        proj4_params = ['+proj=' + 'hammer',
                        '+lon_0=' + str(central_longitude)]

        if false_easting is not None:
            proj4_params.append('+x_0=' + str(false_easting))

        if false_northing is not None:
            proj4_params.append('+y_0=' + str(false_northing))

        crs = crs_factory.createFromParameters('custom', proj4_params)
        JHammer.__init__(self, crs)


class LambertConformal(LambertConformalConic):
    """
    A Lambert Conformal conic projection.
    """

    def __init__(self, central_longitude=-96.0, central_latitude=39.0,
                 false_easting=0.0, false_northing=0.0,
                 standard_parallels=(33, 45), cutoff=-30):
        """
        Parameters
        ----------
        central_longitude: optional
            The central longitude. Defaults to -96.
        central_latitude: optional
            The central latitude. Defaults to 39.
        false_easting: optional
            X offset from planar origin in metres. Defaults to 0.
        false_northing: optional
            Y offset from planar origin in metres. Defaults to 0.
        standard_parallels: optional
            Standard parallel latitude(s). Defaults to (33, 45).
        cutoff: optional
            Latitude of map cutoff.
            The map extends to infinity opposite the central pole
            so we must cut off the map drawing before then.
            A value of 0 will draw half the globe. Defaults to -30.
        """
        proj4_params = ['+proj=' + 'lcc',
                        '+lon_0=' + str(central_longitude),
                        '+lat_0=' + str(central_latitude),
                        '+x_0=' + str(false_easting),
                        '+y_0=' + str(false_northing)]

        n_parallels = len(standard_parallels)

        if not 1 <= n_parallels <= 2:
            raise ValueError('1 or 2 standard parallels must be specified. '
                             'Got {} ({})'.format(n_parallels, standard_parallels))

        proj4_params.append('+lat_1=' + str(standard_parallels[0]))
        if n_parallels == 2:
            proj4_params.append('+lat_2=' + str(standard_parallels[1]))

        crs = crs_factory.createFromParameters('custom', proj4_params)
        LambertConformalConic.__init__(self, crs, cutoff)


class LambertAzimuthalEqualArea(JLambertAzimuthalEqualArea):
    """
    A Lambert Azimuthal Equal-Area projection.
    """

    def __init__(self, central_longitude=0.0, central_latitude=0.0,
                 false_easting=0.0, false_northing=0.0):
        """
        Parameters
        ----------
        central_longitude: optional
            The central longitude. Defaults to 0.
        central_latitude: optional
            The central latitude. Defaults to 0.
        false_easting: optional
            X offset from planar origin in metres. Defaults to 0.
        false_northing: optional
            Y offset from planar origin in metres. Defaults to 0.
        """
        proj4_params = ['+proj=' + 'laea',
                        '+lon_0=' + str(central_longitude),
                        '+lat_0=' + str(central_latitude),
                        '+x_0=' + str(false_easting),
                        '+y_0=' + str(false_northing)]

        crs = crs_factory.createFromParameters('custom', proj4_params)
        JLambertAzimuthalEqualArea.__init__(self, crs)


class LambertEqualArea(LambertEqualAreaConic):
    """
    A Lambert Equal-Area Conic projection.
    """

    def __init__(self, central_longitude=0.0, central_latitude=0.0,
                 false_easting=0.0, false_northing=0.0):
        """
        Parameters
        ----------
        central_longitude: optional
            The central longitude. Defaults to 0.
        central_latitude: optional
            The central latitude. Defaults to 0.
        false_easting: optional
            X offset from planar origin in metres. Defaults to 0.
        false_northing: optional
            Y offset from planar origin in metres. Defaults to 0.
        """
        proj4_params = ['+proj=' + 'leac',
                        '+lon_0=' + str(central_longitude),
                        '+lat_0=' + str(central_latitude),
                        '+x_0=' + str(false_easting),
                        '+y_0=' + str(false_northing)]

        crs = crs_factory.createFromParameters('custom', proj4_params)
        LambertEqualAreaConic.__init__(self, crs)


class Mollweide(JMollweide):
    """
    A Mollweide projection.

    This projection is pseudocylindrical, and equal area. Parallels are
    unequally-spaced straight lines, while meridians are elliptical arcs up to
    semicircles on the edges. Poles are points.

    It is commonly used for world maps, or interrupted with several central
    meridians.

    """

    _handles_ellipses = False

    def __init__(self, central_longitude=0,
                 false_easting=None, false_northing=None):
        """
        Parameters
        ----------
        central_longitude: float, optional
            The central longitude. Defaults to 0.
        false_easting: float, optional
            X offset from planar origin in metres. Defaults to 0.
        false_northing: float, optional
            Y offset from planar origin in metres. Defaults to 0.

            .. note::
                This projection does not handle elliptical globes.

        """
        proj4_params = ['+proj=' + 'moll',
                        '+lon_0=' + str(central_longitude)]

        if false_easting is not None:
            proj4_params.append('+x_0=' + str(false_easting))

        if false_northing is not None:
            proj4_params.append('+y_0=' + str(false_northing))

        crs = crs_factory.createFromParameters('custom', proj4_params)
        JMollweide.__init__(self, crs)


class Orthographic(OrthographicAzimuthal):
    """
    Am orthographic azimuthal projection.
    """

    _handles_ellipses = False

    def __init__(self, central_longitude=0.0, central_latitude=0.0):
        """
        Parameters
        ----------
        central_longitude: optional
            The central longitude. Defaults to 0.
        central_latitude: optional
            The central latitude. Defaults to 0.
        """
        proj4_params = ['+proj=' + 'ortho',
                        '+lon_0=' + str(central_longitude),
                        '+lat_0=' + str(central_latitude)]

        crs = crs_factory.createFromParameters('custom', proj4_params)
        OrthographicAzimuthal.__init__(self, crs)


class Robinson(JRobinson):
    """
    A Robinson projection.

    This projection is pseudocylindrical, and a compromise that is neither
    equal-area nor conformal. Parallels are unequally-spaced straight lines,
    and meridians are curved lines of no particular form.

    It is commonly used for "visually-appealing" world maps.

    """

    _handles_ellipses = False

    def __init__(self, central_longitude=0,
                 false_easting=None, false_northing=None):
        """
        Parameters
        ----------
        central_longitude: float, optional
            The central longitude. Defaults to 0.
        false_easting: float, optional
            X offset from planar origin in metres. Defaults to 0.
        false_northing: float, optional
            Y offset from planar origin in metres. Defaults to 0.

            .. note::
                This projection does not handle elliptical globes.

        """
        proj4_params = ['+proj=' + 'robin',
                        '+lon_0=' + str(central_longitude)]

        if false_easting is not None:
            proj4_params.append('+x_0=' + str(false_easting))

        if false_northing is not None:
            proj4_params.append('+y_0=' + str(false_northing))

        crs = crs_factory.createFromParameters('custom', proj4_params)
        JRobinson.__init__(self, crs)


class Sinusoidal(JSinusoidal):
    """
    A Sinusoidal projection.

    This projection is equal-area.

    """

    def __init__(self, central_longitude=0.0, false_easting=0.0,
                 false_northing=0.0):
        """
        Parameters
        ----------
        central_longitude: optional
            The central longitude. Defaults to 0.
        false_easting: optional
            X offset from planar origin in metres. Defaults to 0.
        false_northing: optional
            Y offset from planar origin in metres. Defaults to 0.

        """
        proj4_params = ['+proj=' + 'sinu',
                        '+lon_0=' + str(central_longitude),
                        '+x_0=' + str(false_easting),
                        '+y_0=' + str(false_northing)]

        crs = crs_factory.createFromParameters('custom', proj4_params)
        JSinusoidal.__init__(self, crs)


class Stereographic(StereographicAzimuthal):
    """
    A stereographic azimuthal projection.
    """

    _wrappable = True

    def __init__(self, central_latitude=0.0, central_longitude=0.0,
                 false_easting=0.0, false_northing=0.0,
                 true_scale_latitude=None, scale_factor=None, cutoff=0):
        proj4_params = ['+proj=' + 'stere',
                        '+lat_0=' + str(central_latitude),
                        '+lon_0=' + str(central_longitude),
                        '+x_0=' + str(false_easting),
                        '+y_0=' + str(false_northing)]

        if true_scale_latitude is not None:
            if central_latitude not in (-90., 90.):
                warnings.warn('"true_scale_latitude" parameter is only used '
                              'for polar stereographic projections. Consider '
                              'the use of "scale_factor" instead.',
                              stacklevel=2)
            proj4_params.append('+lat_ts=' + str(true_scale_latitude))

        if scale_factor is not None:
            if true_scale_latitude is not None:
                raise ValueError('It does not make sense to provide both '
                                 '"scale_factor" and "true_scale_latitude". '
                                 'Ignoring "scale_factor".')
            else:
                proj4_params.append('+k_0=' + str(scale_factor))

        crs = crs_factory.createFromParameters('custom', proj4_params)
        StereographicAzimuthal.__init__(self, crs, cutoff)


class NorthPolarStereo(Stereographic):
    def __init__(self, central_longitude=0.0, true_scale_latitude=None, cutoff=0):
        Stereographic.__init__(self,
            central_latitude=90,
            central_longitude=central_longitude,
            true_scale_latitude=true_scale_latitude,    # None is +90
            cutoff=cutoff)


class SouthPolarStereo(Stereographic):
    def __init__(self, central_longitude=0.0, true_scale_latitude=None, cutoff=0):
        Stereographic.__init__(self,
            central_latitude=-90,
            central_longitude=central_longitude,
            true_scale_latitude=true_scale_latitude,    # None is -90
            cutoff=cutoff)


class Wagner3(JWagner3):
    """
    A Wagner 3 projection.
    """

    def __init__(self, central_longitude=0.0, latitude_true_scale=None,
                 false_easting=0.0, false_northing=0.0):
        """
        Parameters
        ----------
        central_longitude: optional
            The true longitude of the central meridian in degrees.
            Defaults to 0.
        latitude_true_scale: optional
            The latitude where the scale is 1. Defaults to 0 degrees.
        false_easting: optional
            X offset from the planar origin in metres. Defaults to 0.
        false_northing: optional
            Y offset from the planar origin in metres. Defaults to 0.
        """
        proj4_params = ['+proj=wag3',
                        '+lon_0=' + str(central_longitude),
                        '+x_0=' + str(false_easting),
                        '+y_0=' + str(false_northing)]

        # If it's None, we don't pass it to Proj4, in which case its default
        # of 0.0 will be used.
        if latitude_true_scale is not None:
            proj4_params.append('+lat_ts=' + str(latitude_true_scale))

        crs = crs_factory.createFromParameters('custom', proj4_params)
        JWagner3.__init__(self, crs)


class Airy(JAiry):
    """
    An Airy projection.
    """

    def __init__(self, central_longitude=0.0, central_latitude=0.0,
                 false_easting=0.0, false_northing=0.0):
        """
        Parameters
        ----------
        central_longitude: optional
            The central longitude. Defaults to 0.
        central_latitude: optional
            The central latitude. Defaults to 0.
        false_easting: optional
            X offset from planar origin in metres. Defaults to 0.
        false_northing: optional
            Y offset from planar origin in metres. Defaults to 0.
        """
        proj4_params = ['+proj=' + 'airy',
                        '+lat_0=' + str(central_latitude),
                        '+lon_0=' + str(central_longitude),
                        '+x_0=' + str(false_easting),
                        '+y_0=' + str(false_northing)]

        crs = crs_factory.createFromParameters('custom', proj4_params)
        JAiry.__init__(self, crs)


class Aitoff(JAitoff):
    """
    An Aitoff projection.

    This projection is a modified azimuthal equidistant projection, balancing
    shape and scale distortion. There are no standard lines and only the
    central point is free of distortion.

    """

    _handles_ellipses = False

    def __init__(self, central_longitude=0, false_easting=None,
                 false_northing=None):
        """
        Parameters
        ----------
        central_longitude: float, optional
            The central longitude. Defaults to 0.
        false_easting: float, optional
            X offset from planar origin in metres. Defaults to 0.
        false_northing: float, optional
            Y offset from planar origin in metres. Defaults to 0.

            .. note::
                This projection does not handle elliptical globes.

        """
        proj4_params = ['+proj=' + 'aitoff',
                        '+lon_0=' + str(central_longitude)]

        if false_easting is not None:
            proj4_params.append('+x_0=' + str(false_easting))

        if false_northing is not None:
            proj4_params.append('+y_0=' + str(false_northing))

        crs = crs_factory.createFromParameters('custom', proj4_params)
        JAitoff.__init__(self, crs)


class August(JAugust):
    """
    An August projection.

    """

    _handles_ellipses = False

    def __init__(self, central_longitude=0, false_easting=None,
                 false_northing=None):
        """
        Parameters
        ----------
        central_longitude: float, optional
            The central longitude. Defaults to 0.
        false_easting: float, optional
            X offset from planar origin in metres. Defaults to 0.
        false_northing: float, optional
            Y offset from planar origin in metres. Defaults to 0.

            .. note::
                This projection does not handle elliptical globes.

        """
        proj4_params = ['+proj=' + 'august',
                        '+lon_0=' + str(central_longitude)]

        if false_easting is not None:
            proj4_params.append('+x_0=' + str(false_easting))

        if false_northing is not None:
            proj4_params.append('+y_0=' + str(false_northing))

        crs = crs_factory.createFromParameters('custom', proj4_params)
        JAugust.__init__(self, crs)
