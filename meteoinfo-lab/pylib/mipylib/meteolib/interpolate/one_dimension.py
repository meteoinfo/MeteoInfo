"""
Interpolate data along a single axis.

Ported from MetPy.
"""

from org.meteoinfo.ndarray.math import ArrayUtil
import mipylib.numeric as np
from mipylib.numeric.core import NDArray

__all__ = [
    'interpolate_1d','log_interpolate_1d'
    ]

def interpolate_1d(x, xp, *args, **kwargs):
    '''
    Interpolation over a specified axis for arrays of any shape.

    Parameters
    ----------
    x : array-like
        1-D array of desired interpolated values.
    xp : array-like
        The x-coordinates of the data points.
    args : array-like
        The data to be interpolated. Can be multiple arguments, all must be the same shape as
        xp.
    axis : int, optional
        The axis to interpolate over. Defaults to 0.

    Returns
    -------
    array-like
        Interpolated values for each point with coordinates sorted in ascending order.
    '''
    axis = kwargs.pop('axis', 0)
    if isinstance(x, (list, tuple)):
        x = np.array(x)

    if isinstance(x, NDArray):
        x = x._array

    vars = args

    ret = []
    for a in vars:
        r = ArrayUtil.interpolate_1d(x, xp._array, a._array, axis)
        ret.append(NDArray(r))

    if len(ret) == 1:
        return ret[0]
    else:
        return ret

def log_interpolate_1d(x, xp, *args, **kwargs):
    '''
    Interpolation on a logarithmic x-scale for interpolation values in pressure coordintates.

    Parameters
    ----------
    x : array-like
        1-D array of desired interpolated values.
    xp : array-like
        The x-coordinates of the data points.
    args : array-like
        The data to be interpolated. Can be multiple arguments, all must be the same shape as
        xp.
    axis : int, optional
        The axis to interpolate over. Defaults to 0.

    Returns
    -------
    array-like
        Interpolated values for each point with coordinates sorted in ascending order.
    '''
    # Log x and xp
    log_x = np.log(x)
    log_xp = np.log(xp)
    return interpolate_1d(log_x, log_xp, *args, **kwargs)