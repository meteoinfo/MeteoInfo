from org.meteoinfo.math.interpolate import NearestNDInterpolator as JInterp
from mipylib.numeric import NDArray

__all__ = [
    'NearestNDInterpolator'
    ]

class NearestNDInterpolator(object):
    """
    NearestNDInterpolator(x, y)
    Nearest-neighbor interpolation in N dimensions.

    Methods
    -------
    __call__
    Parameters
    ----------
    x : (Npoints, Ndims) ndarray of floats
        Data point coordinates.
    y : (Npoints,) ndarray of float
        Data values.
    """

    def __init__(self, x, y):
        if isinstance(x, (list, tuple)):
            xx = []
            for xi in x:
                xx.append(xi.asarray())
            x = xx
        else:
            x = x.asarray()
        self._interp = JInterp(x, y.asarray())

    def __call__(self, points):
        """
        Evaluate interpolator at given points.
        Parameters
        ----------
        points : ndarray of float, shape (..., ndim)
            Points where to interpolate data at.
        """
        xi = []
        for p in points:
            xi.append(p.asarray())
        r = self._interp.nearest(xi)
        return NDArray(r)