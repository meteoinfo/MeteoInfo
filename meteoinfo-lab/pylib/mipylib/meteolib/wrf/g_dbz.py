from org.meteoinfo.math.meteo import WRF
import mipylib.numeric as np
from mipylib.dataset import DimArray
from .. import constants
from ..calc.thermo import temperature_from_potential_temperature

__all__ = ['get_dbz']

def get_dbz(wrfin, timeidx=0, use_varint=False, use_liqskin=False):
    """Return the simulated radar reflectivity.
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
        use_varint (:obj:`bool`, optional): When set to False,
            the intercept parameters are assumed constant
            (as in MM5's Reisner-2 bulk microphysical scheme).
            When set to True, the variable intercept
            parameters are used as in the more recent version of Reisner-2
            (based on Thompson, Rasmussen, and Manning, 2004, Monthly weather
            Review, Vol. 132, No. 2, pp. 519-542.).
        use_liqskin (:obj:`bool`, optional): When set to True, frozen particles
            that are at a temperature above freezing are assumed to scatter
            as a liquid particle.  Set to False to disable.
    Returns:
        :class:`xarray.DataArray` or :class:`numpy.ndarray`: The simulated
        radar reflectivity.
        If xarray is enabled and the *meta* parameter is True, then the result
        will be a :class:`xarray.DataArray` object.  Otherwise, the result will
        be a :class:`numpy.ndarray` object with no metadata.
    """
    t = wrfin["T"][timeidx]
    p = wrfin["P"][timeidx]
    pb = wrfin["PB"][timeidx]
    qv = wrfin["QVAPOR"][timeidx]
    qr = wrfin["QRAIN"][timeidx]

    if wrfin.varnames.contains("QSNOW"):
        qs = wrfin["QSNOW"][timeidx]
    else:
        qs = np.zeros(qv.shape, qv.dtype)

    if wrfin.varnames.contains("QGRAUP"):
        qg = wrfin["QGRAUP"][timeidx]
    else:
        qg = np.zeros(qv.shape, qv.dtype)

    full_t = t + constants.T_BASE
    full_p = p + pb
    tk = temperature_from_potential_temperature(full_p * 0.01, full_t)

    # If qsnow is not all 0, set sn0 to 1
    sn0 = 1 if qs.any() else 0
    ivarint = 1 if use_varint else 0
    iliqskin = 1 if use_liqskin else 0

    dbz = WRF.calcDBZ(full_p._array, tk._array, qv._array, qr._array, qs._array, qg._array,
                       sn0, ivarint, iliqskin)
    return DimArray(dbz, dims=t.dims)