#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2018-11-23
# Purpose: MeteoInfoLab wrf module
# Note: Jython, some functions revised from wrf-python
#-----------------------------------------------------

from org.meteoinfo.math.meteo import MeteoMath
from mipylib.numeric.core import NDArray, DimArray
import constants as constants
from .calc.thermo import relative_humidity_from_specific_humidity, temperature_from_potential_temperature

__all__ = [
    'destagger','get_slp','get_rh','get_rh2m'
    ]

def destagger(var, stagger_dim):
    '''
    Return the variable on the unstaggered grid.
    
    This function destaggers the variable by taking the average of the 
    values located on either side of the grid box. 
    
    :param var: (*array*) A variable on a staggered grid.
    :param stagger_dim: (*int*) The dimension index to destagger.
        Negative values can be used to choose dimensions referenced 
        from the right hand side (-1 is the rightmost dimension).
        
    :returns: (*array*) The destaggered variable.
    '''
    var_shape = var.shape
    num_dims = var.ndim
    stagger_dim_size = var_shape[stagger_dim]
    # Dynamically building the range slices to create the appropriate 
    # number of ':'s in the array accessor lists.
    # For example, for a 3D array, the calculation would be 
    # result = .5 * (var[:,:,0:stagger_dim_size-2] 
    #                    + var[:,:,1:stagger_dim_size-1])
    # for stagger_dim=2.  So, full slices would be used for dims 0 and 1, but 
    # dim 2 needs the special slice.  
    full_slice = slice(None)
    slice1 = slice(0, stagger_dim_size - 1, 1)
    slice2 = slice(1, stagger_dim_size, 1)
    
    # default to full slices
    dim_ranges_1 = [full_slice] * num_dims
    dim_ranges_2 = [full_slice] * num_dims
    
    # for the stagger dim, insert the appropriate slice range
    dim_ranges_1[stagger_dim] = slice1
    dim_ranges_2[stagger_dim] = slice2
    
    result = .5*(var[tuple(dim_ranges_1)] + var[tuple(dim_ranges_2)])
    
    return result
    
def get_slp(wrfin, timeidx=0, units='hPa'):
    '''
    Return the sea level pressure in the specified units.
    
    This function extracts the necessary variables from the NetCDF file 
    object in order to perform the calculation.
    
    :param wrfin: (*DimDataFile*) Data file.
    :param timeidx: (*int*) Time index.
    :param units: (*string*) The desired units.
    
    :returns: (*array*) Sea level pressure.
    '''
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
    
def get_rh(wrfin, timeidx=0):
    '''
    Return the relative humidity.
    
    This functions extracts the necessary variables from the NetCDF file 
    object in order to perform the calculation.
    
    :param wrfin: (*DimDataFile*) Data file.
    :param timeidx: (*int*) Time index.
    
    :returns: (*array*) Relative humidity.
    '''
    t = wrfin['T'][timeidx,:,:,:]
    p = wrfin['P'][timeidx,:,:,:]
    pb = wrfin['PB'][timeidx,:,:,:]
    qvapor = wrfin['QVAPOR'][timeidx,:,:,:]
    full_t = t + constants.T_BASE
    full_p = p + pb
    qvapor[qvapor < 0] = 0.
    tk = temperature_from_potential_temperature(full_p * 0.01, full_t)
    rh = relative_humidity_from_specific_humidity(qvapor, tk - 273.15, full_p * 0.01) * 100
    
    return rh
    
def get_rh2m(wrfin, timeidx=0):
    '''
    Return the 2m relative humidity.
    
    This functions extracts the necessary variables from the NetCDF file 
    object in order to perform the calculation.
    
    :param wrfin: (*DimDataFile*) Data file.
    :param timeidx: (*int*) Time index.
    
    :returns: (*array*) Relative humidity.
    '''
    t2 = wrfin['T2'][timeidx,:,:]
    psfc = wrfin['PSFC'][timeidx,:,:]
    q2 = wrfin['Q2'][timeidx,:,:]
    q2[q2 < 0] = 0.
    rh = relative_humidity_from_specific_humidity(q2, t2 - 273.15, psfc * 0.01) * 100
    
    return rh