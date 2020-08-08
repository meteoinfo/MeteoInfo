"""Contains calculation of kinematic parameters (e.g. divergence or vorticity)."""

from org.meteoinfo.math.meteo import MeteoMath
import mipylib.numeric as np
from mipylib.numeric.core import NDArray, DimArray

__all__ = [
    'vorticity'
    ]

def vorticity(u, v, x=None, y=None):
    """
    Calculates the vertical component of the curl (ie, vorticity). The data should be lon/lat projection.

    :param u: (*array*) U component array (2D).
    :param v: (*array*) V component array (2D).
    :param x: (*array*) X coordinate array (1D).
    :param y: (*array*) Y coordinate array (1D).

    :returns: Array of the vertical component of the curl.
    """
    if x is None or y is None:
        if isinstance(u, DimArray) and isinstance(v, DimArray):
            x = u.dimvalue(1)
            y = u.dimvalue(0)

        else:
            raise ValueError("Need x, y coordinates")

    if isinstance(x, (list, tuple)):
        x = np.array(x)
    if isinstance(y, (list, tuple)):
        y = np.array(y)

    r = MeteoMath.hcurl(u.asarray(), v.asarray(), x.asarray(), y.asarray())
    return DimArray(NDArray(r), u.dims, u.fill_value, u.proj)
