#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2019-8-12
# Purpose: ncutil module
# Note: Jython
#-----------------------------------------------------

from org.meteoinfo.data.meteodata.netcdf import NCUtil
from ucar.ma2 import Array

from mipylib.numeric.core import NDArray

__all__ = [
    'convert_array'
    ]

def convert_array(a):
    '''
    Convert netcdf Array to NDArray or conversely.
    
    :param a: (*netcdf Array or NDArray*) Input array.
    
    :returns: (*NDArray or netcdf Array) Output array.
    '''
    if isinstance(a, Array):
        return NDArray(NCUtil.convertArray(a))
    else:
        return NCUtil.convertArray(a._array)