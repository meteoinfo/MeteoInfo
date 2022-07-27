from .. import constants
from ..calc.thermo import relative_humidity_from_specific_humidity, temperature_from_potential_temperature

__all__ = ['get_rh', 'get_rh2m']

def get_rh(wrfin, timeidx=0):
    """
    Return the relative humidity.

    This functions extracts the necessary variables from the NetCDF file
    object in order to perform the calculation.

    :param wrfin: (*DimDataFile*) Data file.
    :param timeidx: (*int*) Time index.

    :returns: (*array*) Relative humidity.
    """
    t = wrfin['T'][timeidx,:,:,:]
    p = wrfin['P'][timeidx,:,:,:]
    pb = wrfin['PB'][timeidx,:,:,:]
    qvapor = wrfin['QVAPOR'][timeidx,:,:,:]
    full_t = t + constants.T_BASE
    full_p = p + pb
    qvapor[qvapor < 0] = 0.
    tk = temperature_from_potential_temperature(full_p * 0.01, full_t)
    rh = relative_humidity_from_specific_humidity(full_p * 0.01, tk, qvapor)

    return rh

def get_rh2m(wrfin, timeidx=0):
    """
    Return the 2m relative humidity.

    This functions extracts the necessary variables from the NetCDF file
    object in order to perform the calculation.

    :param wrfin: (*DimDataFile*) Data file.
    :param timeidx: (*int*) Time index.

    :returns: (*array*) Relative humidity.
    """
    t2 = wrfin['T2'][timeidx,:,:]
    psfc = wrfin['PSFC'][timeidx,:,:]
    q2 = wrfin['Q2'][timeidx,:,:]
    q2[q2 < 0] = 0.
    rh = relative_humidity_from_specific_humidity(psfc * 0.01, t2, q2)

    return rh