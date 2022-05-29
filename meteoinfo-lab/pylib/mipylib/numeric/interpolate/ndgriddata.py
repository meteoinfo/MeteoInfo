from org.meteoinfo.math.interpolate import NearestNDInterpolator as JInterp
from org.meteoinfo.math.interpolate import IDWNDInterpolator as JIDWInterp
from mipylib.numeric import NDArray
from jarray import array

__all__ = [
    'NearestNDInterpolator','IDWNDInterpolator'
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

    def __call__(self, points, **kwargs):
        """
        Evaluate interpolator at given points.
        Parameters
        ----------
        points : ndarray of float, shape (..., ndim)
            Points where to interpolate data at.
        nthread : (int)
                  Number of threads.
        """
        if isinstance(points[0], NDArray):
            xi = []
            for p in points:
                xi.append(p.asarray())
            nthread = kwargs.pop('nthread', None)
            if nthread is None:
                r = self._interp.nearest(xi)
            else:
                r = self._interp.nearest(xi, nthread)
            return NDArray(r)
        else:
            points = array(points, 'd')
            r = self._interp.nearest(points)
            return r

class IDWNDInterpolator(object):
    """
    IDWNDInterpolator(x, y, pnum=None, radius=None)
    IDW interpolation in N dimensions.

    Methods
    -------
    __call__
    Parameters
    ----------
    x : (Npoints, Ndims) ndarray of floats
        Data point coordinates.
    y : (Npoints,) ndarray of float
        Data values.
    pnum : (int)
           Points number for interpolation.
    radius : (float)
             Point searching radius.
    wpower : (int)
             Weight power.
    """

    def __init__(self, x, y, pnum=None, radius=None, wpower=None):
        if isinstance(x, (list, tuple)):
            xx = []
            for xi in x:
                xx.append(xi.asarray())
            x = xx
        else:
            x = x.asarray()
        self._interp = JIDWInterp(x, y.asarray())
        if not pnum is None:
            self._interp.setPointNum(pnum)
        if not radius is None:
            self._interp.setRadius(radius)
            if pnum is None:
                self._interp.setPointNum(1)
        if not wpower is None:
            self._interp.setWeightPower(wpower)

    def __call__(self, points, **kwargs):
        """
        Evaluate interpolator at given points.
        Parameters
        ----------
        points : ndarray of float, shape (..., ndim)
            Points where to interpolate data at.
        nthread : (int)
                  Number of threads.
        """
        xi = []
        for p in points:
            xi.append(p.asarray())
        nthread = kwargs.pop('nthread', None)
        if nthread is None:
            r = self._interp.interpolate(xi)
        else:
            r = self._interp.interpolate(xi, nthread)
        return NDArray(r)