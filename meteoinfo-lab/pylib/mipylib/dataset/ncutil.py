#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2019-8-12
# Purpose: ncutil module
# Note: Jython
#-----------------------------------------------------

from org.meteoinfo.data.meteodata.netcdf import NCUtil
from ucar.ma2 import Array, DataType
from ucar.nc2 import Variable

from mipylib.numeric.core import NDArray
from mipylib.numeric.core._dtype import fromjava

__all__ = [
    'convert_array', 'convert_variable', 'to_dtype'
    ]

def convert_array(a):
    """
    Convert netcdf Array to NDArray or conversely.
    
    :param a: (*netcdf Array or NDArray*) Input array.
    
    :returns: (*NDArray or netcdf Array) Output array.
    """
    if isinstance(a, Array):
        return NDArray(NCUtil.convertArray(a))
    else:
        return NCUtil.convertArray(a._array)

def convert_variable(v):
    """
    Convert netcdf variable to MI variable or conversely.

    :param v: (*Variable*) Input variable.
    :return: (*Variable*) Output variable.
    """
    if isinstance(v, Variable):
        return NDArray(NCUtil.convertVariable(v))
    else:
        return NCUtil.convertVariable(v.variable)

def to_dtype(datatype):
    """
    Convert NC DataType to milab dtype.
    :param datatype: (*DataType*) NC DataType
    :return: (*dtype*) milab dtype.
    """
    midtype = NCUtil.convertDataType(datatype)
    return fromjava(midtype)