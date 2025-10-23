from org.meteoinfo.math.meteo import MeteoMath
from mipylib.dataset import DimArray
from .. import constants
from ..calc.thermo import temperature_from_potential_temperature
from destag import destagger

__all__ = ['get_slp']

def get_slp(wrfin, timeidx=0, units='hPa'):
    """
    Return the sea level pressure in the specified units.

    This function extracts the necessary variables from the NetCDF file
    object in order to perform the calculation.

    :param wrfin: (*DimDataFile*) Data file.
    :param timeidx: (*int*) Time index.
    :param units: (*string*) The desired units.

    :returns: (*array*) Sea level pressure.
    """
    t = wrfin['T'][timeidx,:,:,:]
    p = wrfin['P'][timeidx,:,:,:]
    pb = wrfin['PB'][timeidx,:,:,:]
    qvapor = wrfin['QVAPOR'][timeidx,:,:,:]
    ph = wrfin['PH'][timeidx,:,:,:]
    phb = wrfin['PHB'][timeidx,:,:,:]
    full_t = t + constants.T_BASE
    full_p = p + pb
    qvapor[qvapor < 0] = 0.

    full_ph = (ph + phb) / constants.g
    destag_ph = destagger(full_ph, -3)
    tk = temperature_from_potential_temperature(full_p * 0.01, full_t)
    slp = MeteoMath.calSeaPrs(destag_ph._array, tk._array, full_p._array, qvapor._array)

    return DimArray(slp, dims=t.dims[1:])