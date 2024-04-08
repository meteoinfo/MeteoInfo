__all__ = ['get_pressure']

def get_pressure(wrfin, timeidx=0):
    """
    Return the pressure in the specified units.

    This function extracts the necessary variables from the NetCDF file
    object in order to perform the calculation.

    :param wrfin: (*DimDataFile*) Data file.
    :param timeidx: (*int*) Time index. Default is `0`.

    :returns: (*array*) Pressure (hPa).
    """
    p = wrfin['P'][timeidx]
    pb = wrfin['PB'][timeidx]
    pres = (p + pb) * 0.01

    return pres