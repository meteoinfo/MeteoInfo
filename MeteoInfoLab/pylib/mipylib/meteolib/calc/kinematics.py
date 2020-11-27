"""Contains calculation of kinematic parameters (e.g. divergence or vorticity)."""

from org.meteoinfo.math.meteo import MeteoMath
import mipylib.numeric as np
from mipylib.numeric.core import NDArray, DimArray

__all__ = [
    'cdiff','divergence','vorticity'
    ]

def cdiff(a, dimidx):
    '''
    Performs a centered difference operation on a array in a specific direction

    :param a: (*array*) The input array.
    :param dimidx: (*int*) Demension index of the specific direction.

    :returns: Result array.
    '''
    r = MeteoMath.cdiff(a.asarray(), dimidx)
    if isinstance(a, DimArray):
        return DimArray(NDArray(r), a.dims, a.fill_value, a.proj)
    else:
        return NDArray(r)

def vorticity(u, v, x=None, y=None):
    """
    Calculates the vertical component of the curl (ie, vorticity). The data should be lon/lat projection.

    :param u: (*array*) U component array (2D).
    :param v: (*array*) V component array (2D).
    :param x: (*array*) X coordinate array (1D).
    :param y: (*array*) Y coordinate array (1D).

    :returns: Array of the vertical component of the curl.
    """
    ny = u.shape[-2]
    nx = u.shape[-1]
    if x is None:
        if isinstance(u, DimArray):
            x = u.dimvalue(-1)
        else:
            x = np.arange(nx)
    elif isinstance(x, (list, tuple)):
        x = np.array(x)

    if y is None:
        if isinstance(v, DimArray):
            y = u.dimvalue(-2)
        else:
            y = np.arange(ny)
    elif isinstance(y, (list, tuple)):
        y = np.array(y)

    r = MeteoMath.vorticity(u.asarray(), v.asarray(), x.asarray(), y.asarray())
    return DimArray(NDArray(r), u.dims, u.fill_value, u.proj)

def divergence(u, v, x=None, y=None):
    '''
    Calculates the horizontal divergence using finite differencing. The data should be lon/lat projection.

    :param u: (*array*) U component array.
    :param v: (*array*) V component array.
    :param x: (*array*) X coordinate.
    :param y: (*array*) Y coordinate.

    :returns: Array of the horizontal divergence.
    '''
    ny = u.shape[-2]
    nx = u.shape[-1]
    if x is None:
        if isinstance(u, DimArray):
            x = u.dimvalue(-1)
        else:
            x = np.arange(nx)
    elif isinstance(x, (list, tuple)):
        x = np.array(x)

    if y is None:
        if isinstance(v, DimArray):
            y = u.dimvalue(-2)
        else:
            y = np.arange(ny)
    elif isinstance(y, (list, tuple)):
        y = np.array(y)

    r = MeteoMath.divergence(u.asarray(), v.asarray(), x.asarray(), y.asarray())
    if isinstance(u, DimArray):
        return DimArray(NDArray(r), u.dims, u.fill_value, u.proj)
    else:
        return NDArray(r)