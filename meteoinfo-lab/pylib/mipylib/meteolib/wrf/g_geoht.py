from util import either
from destag import destagger
from .. import constants
import warnings

__all__ = ['get_geopt', 'get_stag_geopt', 'get_height', 'get_stag_height', 'get_height_agl']

def _get_geoht(wrfin, timeidx, height=True, msl=True, stag=False):
    """Return the geopotential or geopotential height.

    If *height* is False, then geopotential is returned in units of
    [m2 s-2].  If *height* is True, then geopotential height is
    returned in units of [m].  If *msl* is True, then geopotential height
    is return as Mean Sea Level (MSL).  If *msl* is False, then geopotential
    height is returned as Above Ground Level (AGL).
    This functions extracts the necessary variables from the NetCDF file
    object in order to perform the calculation.

    Args:
        wrfin (:class:`netCDF4.Dataset`, :class:`Nio.NioFile`, or an \
            iterable): WRF-ARW NetCDF
            data as a :class:`netCDF4.Dataset`, :class:`Nio.NioFile`
            or an iterable sequence of the aforementioned types.
        timeidx (:obj:`int` or :data:`wrf.ALL_TIMES`, optional): The
            desired time index. This value can be a positive integer,
            negative integer, or
            :data:`wrf.ALL_TIMES` (an alias for None) to return
            all times in the file or sequence. The default is 0.
        height (:obj:`bool`, optional): Set to True to return geopotential
            height instead of geopotential.  Default is True.
        msl (:obj:`bool`, optional): Set to True to return geopotential height
            as Mean Sea Level (MSL).  Set to False to return the
            geopotential height as Above Ground Level (AGL) by subtracting
            the terrain height.  Default is True.
        stag (:obj:`bool`, optional): Set to True to use the vertical
            staggered grid, rather than the mass grid. Default is False.

    Returns:
        :class:`DimArray`
    """

    varname = either("PH", "GHT")(wrfin)
    if varname == "PH":
        ph = wrfin["PH"][timeidx]
        phb = wrfin["PHB"][timeidx]
        hgt = wrfin["HGT"][timeidx]
        geopt = ph + phb
        if not stag:
            geopt_unstag = destagger(geopt, -3)
        else:
            geopt_unstag = geopt
    else:
        geopt_unstag = wrfin["GHT"][timeidx] * constants.g
        hgt = wrfin["HGT_M"][timeidx]

        if stag:
            warnings.warn("file contains no vertically staggered geopotential "
                          "height variable, returning unstaggered result "
                          "instead")
    if height:
        if msl:
            return geopt_unstag / constants.g
        else:
            # Due to broadcasting with multifile/multitime, the 2D terrain
            # array needs to be reshaped to a 3D array so the right dims
            # line up
            new_dims = list(hgt.shape)
            new_dims.insert(-2, 1)
            hgt = hgt.reshape(new_dims)

            return (geopt_unstag / constants.g) - hgt
    else:
        return geopt_unstag

def get_geopt(wrfin, timeidx=0):
    """Return the geopotential.

    The geopotential is returned in units of [m2 s-2].
    This functions extracts the necessary variables from the NetCDF file
    object in order to perform the calculation.

    Args:
        wrfin (:class:`netCDF4.Dataset`, :class:`Nio.NioFile`, or an \
            iterable): WRF-ARW NetCDF
            data as a :class:`netCDF4.Dataset`, :class:`Nio.NioFile`
            or an iterable sequence of the aforementioned types.
        timeidx (:obj:`int` or :data:`wrf.ALL_TIMES`, optional): The
            desired time index. This value can be a positive integer,
            negative integer, or
            :data:`wrf.ALL_TIMES` (an alias for None) to return
            all times in the file or sequence. The default is 0.

    Returns:
        :class:`DimArray`
    """
    return _get_geoht(wrfin, timeidx, False, True)

def get_height(wrfin, timeidx=0, msl=True):
    """Return the geopotential height.

    If *msl* is True, then geopotential height is returned as Mean Sea Level
    (MSL).  If *msl* is False, then geopotential height is returned as
    Above Ground Level (AGL) by subtracting the terrain height.
    This functions extracts the necessary variables from the NetCDF file
    object in order to perform the calculation.

    Args:
        wrfin (:class:`netCDF4.Dataset`, :class:`Nio.NioFile`, or an \
            iterable): WRF-ARW NetCDF
            data as a :class:`netCDF4.Dataset`, :class:`Nio.NioFile`
            or an iterable sequence of the aforementioned types.
        timeidx (:obj:`int` or :data:`wrf.ALL_TIMES`, optional): The
            desired time index. This value can be a positive integer,
            negative integer, or
            :data:`wrf.ALL_TIMES` (an alias for None) to return
            all times in the file or sequence. The default is 0.
        msl (:obj:`bool`, optional): Set to True to return geopotential height
            as Mean Sea Level (MSL).  Set to False to return the
            geopotential height as Above Ground Level (AGL) by subtracting
            the terrain height.  Default is True.

    Returns:
        :class:`DimArray` or :class:`numpy.ndarray`: The
        geopotential height (m).
    """
    return _get_geoht(wrfin, timeidx, True, msl)

def get_stag_geopt(wrfin, timeidx=0):
    """Return the geopotential for the vertically staggered grid.

    The geopotential is returned in units of [m2 s-2].
    This functions extracts the necessary variables from the NetCDF file
    object in order to perform the calculation.

    Args:
        wrfin (:class:`netCDF4.Dataset`, :class:`Nio.NioFile`, or an \
            iterable): WRF-ARW NetCDF
            data as a :class:`netCDF4.Dataset`, :class:`Nio.NioFile`
            or an iterable sequence of the aforementioned types.
        timeidx (:obj:`int` or :data:`wrf.ALL_TIMES`, optional): The
            desired time index. This value can be a positive integer,
            negative integer, or
            :data:`wrf.ALL_TIMES` (an alias for None) to return
            all times in the file or sequence. The default is 0.

    Returns:
        :class:`DimArray` or :class:`numpy.ndarray`: The
        geopotential (m).
    """
    return _get_geoht(wrfin, timeidx, False, True, stag=True)


def get_stag_height(wrfin, timeidx=0, msl=True):
    """Return the geopotential height for the vertically staggered grid.

    If *msl* is True, then geopotential height is returned as Mean Sea Level
    (MSL).  If *msl* is False, then geopotential height is returned as
    Above Ground Level (AGL) by subtracting the terrain height.
    This functions extracts the necessary variables from the NetCDF file
    object in order to perform the calculation.

    Args:
        wrfin (:class:`netCDF4.Dataset`, :class:`Nio.NioFile`, or an \
            iterable): WRF-ARW NetCDF
            data as a :class:`netCDF4.Dataset`, :class:`Nio.NioFile`
            or an iterable sequence of the aforementioned types.
        timeidx (:obj:`int` or :data:`wrf.ALL_TIMES`, optional): The
            desired time index. This value can be a positive integer,
            negative integer, or
            :data:`wrf.ALL_TIMES` (an alias for None) to return
            all times in the file or sequence. The default is 0.
        msl (:obj:`bool`, optional): Set to True to return geopotential height
            as Mean Sea Level (MSL).  Set to False to return the
            geopotential height as Above Ground Level (AGL) by subtracting
            the terrain height.  Default is True.

    Returns:
        :class:`DimArray` or :class:`numpy.ndarray`: The
        geopotential height (m).
    """

    return _get_geoht(wrfin, timeidx, True, msl, stag=True)


def get_height_agl(wrfin, timeidx=0):
    """Return the geopotential height (AGL).

    The geopotential height is returned as Above Ground Level (AGL) by
    subtracting the terrain height.
    This functions extracts the necessary variables from the NetCDF file
    object in order to perform the calculation.

    Args:
        wrfin (:class:`netCDF4.Dataset`, :class:`Nio.NioFile`, or an \
            iterable): WRF-ARW NetCDF
            data as a :class:`netCDF4.Dataset`, :class:`Nio.NioFile`
            or an iterable sequence of the aforementioned types.
        timeidx (:obj:`int` or :data:`wrf.ALL_TIMES`, optional): The
            desired time index. This value can be a positive integer,
            negative integer, or
            :data:`wrf.ALL_TIMES` (an alias for None) to return
            all times in the file or sequence. The default is 0.

    Returns:
        :class:`DimArray` or :class:`numpy.ndarray`: The
        geopotential height (m).
    """

    return _get_geoht(wrfin, timeidx, True, False)